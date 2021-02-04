package agent.invoke.proxy;


import agent.invoke.DestInvoke;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

import static agent.invoke.proxy.ProxyPosition.*;

public class ProxyCallSite {
    private static final Map<ProxyPosition, Function<ProxyCallInfo, ProxyCall>> posToProxyClass = new HashMap<>();

    static {
        posToProxyClass.put(ON_BEFORE, ProxyCallBefore::new);
        posToProxyClass.put(ON_RETURNING, ProxyCallOnReturning::new);
        posToProxyClass.put(ON_THROWING, ProxyCallOnThrowing::new);
        posToProxyClass.put(ON_CATCHING, ProxyCallOnCatching::new);
        posToProxyClass.put(ON_AFTER, ProxyCallAfter::new);

        // use to resolve linkage error
        CallQueue.class.getName();
    }

    private DestInvoke destInvoke;
    private final Map<ProxyPosition, CallQueue> posToQueue = new ConcurrentHashMap<>();

    public ProxyCallSite(DestInvoke destInvoke) {
        this.destInvoke = destInvoke;
    }

    public Map<String, List<String>> getPosToDisplayStrings() {
        Map<String, List<String>> rsMap = new TreeMap<>();
        posToQueue.forEach(
                (pos, queue) -> {
                    if (!queue.calls.isEmpty())
                        rsMap.put(
                                pos.toString(),
                                queue.calls.stream()
                                        .map(ProxyCall::getCallInfo)
                                        .map(ProxyCallInfo::getTid)
                                        .collect(
                                                Collectors.toList()
                                        )
                        );
                }
        );
        return rsMap;
    }

    DestInvoke getDestInvoke() {
        return destInvoke;
    }

    public void reg(ProxyPosition pos, Collection<ProxyCallInfo> proxyCallInfos) {
        CallQueue queue = getQueue(pos);
        proxyCallInfos.forEach(queue::add);
    }

    public boolean isEmpty() {
        return posToQueue.values().stream().allMatch(CallQueue::isEmpty);
    }

    private CallQueue getQueue(ProxyPosition pos) {
        return posToQueue.computeIfAbsent(
                pos,
                key -> new CallQueue(pos)
        );
    }

    private void invoke(ProxyPosition pos, Object instanceOrNull, Object pv) {
        getQueue(pos).calls.forEach(
                proxyCall -> proxyCall.run(destInvoke, instanceOrNull, pv)
        );
    }

    public void removeCalls(Collection<String> tids) {
        posToQueue.forEach(
                (pos, queue) -> queue.remove(tids)
        );
    }

    public boolean containsCall(String tid) {
        for (CallQueue queue : posToQueue.values()) {
            if (queue.contains(tid))
                return true;
        }
        return false;
    }

    public void invokeBefore(Object instanceOrNull, Object pv) {
        invoke(ON_BEFORE, instanceOrNull, pv);
    }

    public void invokeOnReturning(Object instanceOrNull, Object pv) {
        invoke(ON_RETURNING, instanceOrNull, pv);
        invoke(ON_AFTER, instanceOrNull, null);
    }

    public void invokeOnThrowing(Object instanceOrNull, Object pv) {
        invoke(ON_THROWING, instanceOrNull, pv);
        invoke(ON_AFTER, instanceOrNull, null);
    }

    public void invokeOnCatching(Object instanceOrNull, Object pv) {
        invoke(ON_CATCHING, instanceOrNull, pv);
    }

    private static class CallQueue {
        private final Queue<ProxyCall> calls = new ConcurrentLinkedQueue<>();
        private final Map<String, ProxyCall> tidToCall = new ConcurrentHashMap<>();
        private final ProxyPosition pos;

        private CallQueue(ProxyPosition pos) {
            this.pos = pos;
        }

        private boolean isEmpty() {
            return calls.isEmpty();
        }

        private boolean contains(String tid) {
            return tidToCall.containsKey(tid);
        }

        private void remove(Collection<String> tids) {
            tids.forEach(
                    tid -> {
                        ProxyCall call = tidToCall.remove(tid);
                        if (call != null)
                            calls.remove(call);
                    }
            );
        }

        private void add(ProxyCallInfo callInfo) {
            tidToCall.computeIfAbsent(
                    callInfo.getTid(),
                    tid -> {
                        ProxyCall call = newProxyCall(callInfo);
                        calls.add(call);
                        return call;
                    }
            );
        }

        private ProxyCall newProxyCall(ProxyCallInfo callInfo) {
            return Optional.ofNullable(
                    posToProxyClass.get(pos)
            ).map(
                    func -> func.apply(callInfo)
            ).orElseThrow(
                    () -> new IllegalArgumentException("Invalid proxy position: " + pos)
            );
        }
    }
}
