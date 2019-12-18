package agent.server.transform.tools.asm;

import agent.base.utils.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyTransformMgr {
    private static final Logger logger = Logger.getLogger(ProxyTransformMgr.class);
    private static final ProxyTransformMgr instance = new ProxyTransformMgr();
    private static final Object[] noArgs = new Object[0];

    private AtomicInteger idGenerator = new AtomicInteger(0);
    private Map<DestInvoke, Integer> invokeToId = new HashMap<>();
    private Map<Integer, ProxyCallSite> idToCallSite = new HashMap<>();

    public static ProxyTransformMgr getInstance() {
        return instance;
    }

    private ProxyTransformMgr() {
    }

    public synchronized List<ProxyResult> transform(Collection<ProxyRegInfo> regInfos, Function<Class<?>, byte[]> classDataFunc) {
        Map<Class<?>, ProxyItem> classToItem = new HashMap<>();
        regInfos.forEach(
                regInfo -> {
                    DestInvoke destInvoke = regInfo.getDestInvoke();
                    Integer invokeId = invokeToId.get(destInvoke);
                    if (invokeId == null) {
                        final Integer newId = idGenerator.incrementAndGet();
                        invokeToId.put(destInvoke, newId);
                        classToItem.computeIfAbsent(
                                destInvoke.getDeclaringClass(),
                                ProxyItem::new
                        ).reg(newId, destInvoke, regInfo);
                    } else {
                        reg(
                                destInvoke,
                                Collections.singleton(regInfo)
                        );
                    }
                }
        );

        return classToItem.isEmpty() ?
                Collections.emptyList() :
                classToItem.values()
                        .stream()
                        .map(
                                item -> doTransform(item, classDataFunc)
                        )
                        .collect(
                                Collectors.toList()
                        );
    }

    private ProxyResult doTransform(ProxyItem item, Function<Class<?>, byte[]> classDataFunc) {
        byte[] newClassData = AsmTransformProxy.transform(
                classDataFunc.apply(item.clazz),
                item.idToInvoke
        );
        String verifyResult = AsmUtils.getVerifyResult(
                item.clazz.getClassLoader(),
                newClassData,
                false
        );
        if (!verifyResult.isEmpty()) {
            logger.error("Verify {} transform failed: \n{}\n=================\n{}",
                    item.clazz.getName(),
                    AsmUtils.getVerifyResult(
                            item.clazz.getClassLoader(),
                            newClassData,
                            true
                    ),
                    AsmUtils.convertToReadableContent(newClassData)
            );
            return new ProxyResult(
                    item.clazz,
                    new RuntimeException("Verify transform failed.")
            );
        }
        return new ProxyResult(
                item.clazz,
                newClassData,
                item.invokeToRegInfos
        );
    }

    public synchronized void reg(Collection<ProxyResult> results) {
        results.forEach(
                result -> result.getInvokeToRegInfos().forEach(this::reg)
        );
    }

    private void reg(DestInvoke destInvoke, Collection<ProxyRegInfo> regInfos) {
        if (!regInfos.isEmpty()) {
            int invokeId = Optional.ofNullable(
                    invokeToId.get(destInvoke)
            ).orElseThrow(
                    () -> new RuntimeException("No id found for destInvoke: " + destInvoke)
            );
            ProxyCallSite callConfig = idToCallSite.computeIfAbsent(
                    invokeId,
                    key -> new ProxyCallSite(destInvoke)
            );
            regInfos.forEach(
                    regInfo -> regInfo.getPosToCalInfos().forEach(callConfig::reg)
            );
        }
    }

    private ProxyCallSite getCallSite(int invokeId) {
        return Optional.ofNullable(
                idToCallSite.get(invokeId)
        ).orElseThrow(
                () -> new RuntimeException("No call config found by destInvoke id: " + invokeId)
        );
    }

    public void onBefore(int invokeId, Object instanceOrNull, Object[] args) throws Throwable {
        getCallSite(invokeId).invokeBefore(instanceOrNull, args);
    }

    public void onReturning(int invokeId, Object instanceOrNull, Object returnValue) throws Throwable {
        getCallSite(invokeId).invokeOnReturning(instanceOrNull, returnValue);
    }

    public void onThrowing(int invokeId, Object instanceOrNull, Throwable error) throws Throwable {
        getCallSite(invokeId).invokeOnThrowing(instanceOrNull, error);
    }

    private static class ProxyItem {
        private final Class<?> clazz;
        private Map<Integer, DestInvoke> idToInvoke = new HashMap<>();
        private Map<DestInvoke, List<ProxyRegInfo>> invokeToRegInfos = new HashMap<>();

        ProxyItem(Class<?> clazz) {
            this.clazz = clazz;
        }

        void reg(Integer invokeId, DestInvoke destInvoke, ProxyRegInfo regInfo) {
            idToInvoke.put(invokeId, destInvoke);
            invokeToRegInfos.computeIfAbsent(
                    destInvoke,
                    key -> new ArrayList<>()
            ).add(regInfo);
        }
    }
}
