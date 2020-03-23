package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static agent.server.transform.tools.asm.ProxyPosition.*;

class ProxyCallSite {
    private static final Map<ProxyPosition, Class<? extends ProxyCall>> posToProxyClass = new HashMap<>();

    static {
        posToProxyClass.put(ON_BEFORE, ProxyCallBefore.class);
        posToProxyClass.put(ON_RETURNING, ProxyCallOnReturning.class);
        posToProxyClass.put(ON_THROWING, ProxyCallOnThrowing.class);
        posToProxyClass.put(ON_AFTER, ProxyCallAfter.class);
    }

    private DestInvoke destInvoke;
    private final Map<ProxyPosition, CallQueue> posToQueue = new ConcurrentHashMap<>();

    ProxyCallSite(DestInvoke destInvoke) {
        this.destInvoke = destInvoke;
    }

    Map<String, List<String>> getPosToDisplayStrings() {
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

    void reg(ProxyPosition pos, Collection<ProxyCallInfo> proxyCallInfos) {
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

    void invokeBefore(Object instanceOrNull, Object pv) {
        invoke(ON_BEFORE, instanceOrNull, pv);
    }

    void invokeOnReturning(Object instanceOrNull, Object pv) {
        invoke(ON_RETURNING, instanceOrNull, pv);
        invoke(ON_AFTER, instanceOrNull, null);
    }

    void invokeOnThrowing(Object instanceOrNull, Object pv) {
        invoke(ON_THROWING, instanceOrNull, pv);
        invoke(ON_AFTER, instanceOrNull, null);
    }

    private static class CallQueue {
        private final Queue<ProxyCall> calls = new ConcurrentLinkedQueue<>();
//        private final Set<String> tags = new HashSet<>();

        private void add(ProxyPosition pos, ProxyCallInfo callInfo) {
//            synchronized (this) {
//                String tag = callInfo.getTag();
//                if (tags.contains(tag))
//                    return;
//                tags.add(tag);
//            }
            calls.add(
                    newProxyCall(pos, callInfo)
            );
        }

        private ProxyCall newProxyCall(ProxyPosition pos, ProxyCallInfo callInfo) {
            Class<?> clazz = Optional.ofNullable(
                    posToProxyClass.get(pos)
            ).orElseThrow(
                    () -> new IllegalArgumentException("Invalid proxy position: " + pos)
            );
            return Utils.wrapToRtError(
                    () -> ReflectionUtils.newInstance(
                            clazz,
                            new Class[]{
                                    ProxyCallInfo.class
                            },
                            callInfo
                    )
            );
        }
    }
}
