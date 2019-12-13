package agent.server.transform.tools.asm;

import java.util.*;
import java.util.stream.Stream;

import static agent.server.transform.tools.asm.ProxyPosition.*;

class ProxyCallConfig {
    private DestInvoke destInvoke;
    private final Map<ProxyPosition, LinkedList<ProxyCall>> posToQueue = new HashMap<>();

    ProxyCallConfig(DestInvoke destInvoke) {
        this.destInvoke = destInvoke;
        this.init();
    }

    DestInvoke getDestInvoke() {
        return destInvoke;
    }

    private void init() {
        Stream.of(
                ProxyPosition.values()
        ).forEach(
                pos -> posToQueue.put(
                        pos,
                        new LinkedList<>()
                )
        );
    }

    synchronized void reg(ProxyPosition proxyPosition, Collection<ProxyCallInfo> proxyCallInfos) {
        Optional.ofNullable(
                posToQueue.get(proxyPosition)
        ).ifPresent(
                queue -> proxyCallInfos.forEach(
                        callInfo -> add(queue, proxyPosition, callInfo)
                )
        );
    }

    private void add(LinkedList<ProxyCall> queue, ProxyPosition position, ProxyCallInfo callInfo) {
        switch (position) {
            case BEFORE:
                queue.add(
                        new ProxyCallBefore(callInfo)
                );
                break;
            case AFTER:
                queue.addFirst(
                        new ProxyCallAfter(callInfo)
                );
                break;
            case AFTER_RETURNING:
                queue.addFirst(
                        new ProxyCallAfterReturning(callInfo)
                );
                break;
            case AFTER_THROWING:
                queue.addFirst(
                        new ProxyCallAfterThrowing(callInfo)
                );
                break;
            case AROUND:
                queue.add(
                        new ProxyCallAround(callInfo)
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    Queue<ProxyCall> getBeforeQueue() {
        return getQueue(BEFORE);
    }

    Queue<ProxyCall> getAfterQueue() {
        return getQueue(AFTER);
    }

    Queue<ProxyCall> getAroundQueue() {
        return getQueue(AROUND);
    }

    Queue<ProxyCall> getAfterReturningQueue() {
        return getQueue(AFTER_RETURNING);
    }

    Queue<ProxyCall> getAfterThrowingQueue() {
        return getQueue(AFTER_THROWING);
    }

    private Queue<ProxyCall> getQueue(ProxyPosition pos) {
        return posToQueue.get(pos);
    }

}
