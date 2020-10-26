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
                                        .map(ProxyCallInfo::getTag)
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
        proxyCallInfos.forEach(
                callInfo -> queue.add(pos, callInfo)
        );
    }

    private CallQueue getQueue(ProxyPosition pos) {
        return posToQueue.computeIfAbsent(
                pos,
                key -> new CallQueue()
        );
    }

    private void invoke(ProxyPosition pos, Object instanceOrNull, Object pv) {
        getQueue(pos).calls.forEach(
                proxyCall -> proxyCall.run(destInvoke, instanceOrNull, pv)
        );
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
        private final Set<String> tags = new HashSet<>();

        private void add(ProxyPosition pos, ProxyCallInfo callInfo) {
            synchronized (this) {
                String tag = callInfo.getTag();
                if (tags.contains(tag))
                    return;
                tags.add(tag);
            }
            calls.add(
                    newProxyCall(pos, callInfo)
            );
        }

        private ProxyCall newProxyCall(ProxyPosition pos, ProxyCallInfo callInfo) {
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
