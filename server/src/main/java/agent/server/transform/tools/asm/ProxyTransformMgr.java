package agent.server.transform.tools.asm;

import agent.base.utils.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyTransformMgr {
    private static final Logger logger = Logger.getLogger(ProxyTransformMgr.class);
    private static final ProxyTransformMgr instance = new ProxyTransformMgr();

    private AtomicInteger idGenerator = new AtomicInteger(0);
    private Map<DestInvoke, Integer> invokeToId = new HashMap<>();
    private Map<Integer, ProxyCallConfig> idToCallConfig = new HashMap<>();
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
        byte[] newClassData = NewAsmTransformProxy.transform(
                classDataFunc.apply(item.clazz),
                item.idToInvoke
        );
        String verifyResult = AsmUtils.getVerifyResult(newClassData, false);
        if (!verifyResult.isEmpty()) {
            logger.error("Verify {} transform failed: \n{}",
                    item.clazz.getName(),
                    AsmUtils.getVerifyResult(newClassData, true)
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
            ProxyCallConfig callConfig = idToCallConfig.computeIfAbsent(
                    invokeId,
                    key -> new ProxyCallConfig(destInvoke)
            );
            regInfos.forEach(
                    regInfo -> regInfo.getPosToCalInfos().forEach(callConfig::reg)
            );
        }
    }

    public Object onDelegate(Object target, Class<?> targetClass, String targetMethodName, int invokeId, Object[] args) throws Throwable {
        ProxyCallConfig callConfig = Optional.ofNullable(
                idToCallConfig.get(invokeId)
        ).orElseThrow(
                () -> new RuntimeException("No call config found by destInvoke id: " + invokeId)
        );
        return idToCallSite.computeIfAbsent(
                invokeId,
                key -> callConfig.getDestInvoke().newCallSite(
                        new ProxyCallSiteConfig(
                                callConfig,
                                targetClass,
                                targetMethodName
                        )
                )
        ).invoke(target, args);
    }

    private static class ProxyItem {
        private final Class<?> clazz;
        private Map<Integer, DestInvoke> idToInvoke = new HashMap<>();
        private Map<DestInvoke, List<ProxyRegInfo>> invokeToRegInfos = new HashMap<>();

        private ProxyItem(Class<?> clazz) {
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
