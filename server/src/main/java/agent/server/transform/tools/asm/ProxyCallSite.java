package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final Map<ProxyPosition, Queue<ProxyCall>> posToQueue = new HashMap<>();

    ProxyCallSite(DestInvoke destInvoke) {
        this.destInvoke = destInvoke;
        this.init();
    }

    Map<String, List<String>> getPosToDisplayStrings() {
        Map<String, List<String>> rsMap = new TreeMap<>();
        posToQueue.forEach(
                (pos, queue) -> {
                    if (!queue.isEmpty())
                        rsMap.put(
                                pos.toString(),
                                queue.stream()
                                        .map(ProxyCall::getDisplayString)
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

    private void init() {
        Stream.of(
                ProxyPosition.values()
        ).forEach(
                pos -> posToQueue.put(
                        pos,
                        new ConcurrentLinkedQueue<>()
                )
        );
    }

    void reg(ProxyPosition pos, Collection<ProxyCallInfo> proxyCallInfos) {
        Queue<ProxyCall> queue = getQueue(pos);
        proxyCallInfos.forEach(
                callInfo -> queue.add(
                        newProxyCall(pos, callInfo)
                )
        );
    }

    private Queue<ProxyCall> getQueue(ProxyPosition pos) {
        return Optional.ofNullable(
                posToQueue.get(pos)
        ).orElseThrow(
                () -> new IllegalArgumentException("Invalid proxy position: " + pos)
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

    private void invoke(ProxyPosition pos, Object instanceOrNull, Object pv) {
        getQueue(pos).forEach(
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
}
