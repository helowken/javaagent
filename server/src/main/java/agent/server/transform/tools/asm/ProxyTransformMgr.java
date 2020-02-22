package agent.server.transform.tools.asm;

import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.Logger;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyTransformMgr {
    private static final Logger logger = Logger.getLogger(ProxyTransformMgr.class);
    private static final ProxyTransformMgr instance = new ProxyTransformMgr();

    private Map<Integer, ProxyCallSite> idToCallSite = new ConcurrentHashMap<>();

    public static ProxyTransformMgr getInstance() {
        return instance;
    }

    private ProxyTransformMgr() {
    }

    public Map<String, List<String>> getCallSiteDisplay(Integer invokeId) {
        return getCallSite(invokeId).getPosToDisplayStrings();
    }

    public List<ProxyResult> transform(Collection<ProxyRegInfo> regInfos, Function<Class<?>, byte[]> classDataFunc) {
        Map<Class<?>, ProxyItem> classToItem = new HashMap<>();
        regInfos.forEach(
                regInfo -> {
                    DestInvoke destInvoke = regInfo.getDestInvoke();
                    Integer invokeId = DestInvokeIdRegistry.getInstance().get(destInvoke);
                    idToCallSite.compute(
                            invokeId,
                            (key, oldValue) -> {
                                if (oldValue == null) {
                                    classToItem.computeIfAbsent(
                                            destInvoke.getDeclaringClass(),
                                            ProxyItem::new
                                    ).reg(invokeId, destInvoke, regInfo);
                                    return new ProxyCallSite(destInvoke);
                                } else {
                                    reg(
                                            invokeId,
                                            Collections.singleton(regInfo)
                                    );
                                    return oldValue;
                                }
                            }
                    );
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
        Class<?> targetClass = item.getTargetClass();
        byte[] newClassData = AsmTransformProxy.transform(
                classDataFunc.apply(targetClass),
                item.getIdToInvoke()
        );
        String verifyResult = AsmUtils.getVerifyResult(
                targetClass.getClassLoader(),
                newClassData,
                false
        );
        if (!verifyResult.isEmpty()) {
            logger.error("Verify {} transform failed: \n{}\n=================\n{}",
                    targetClass.getName(),
                    AsmUtils.getVerifyResult(
                            targetClass.getClassLoader(),
                            newClassData,
                            true
                    ),
                    AsmUtils.convertToReadableContent(newClassData)
            );
            return new ProxyResult(
                    item,
                    new RuntimeException("Verify transform failed.")
            );
        }
        return new ProxyResult(
                item,
                newClassData
        );
    }

    public void reg(Collection<ProxyResult> results) {
        results.forEach(
                result -> result.getIdToRegInfos().forEach(this::reg)
        );
    }

    private void reg(Integer invokeId, Collection<ProxyRegInfo> regInfos) {
        if (!regInfos.isEmpty()) {
            ProxyCallSite callSite = getCallSite(invokeId);
            regInfos.forEach(
                    regInfo -> regInfo.getPosToCalInfos().forEach(callSite::reg)
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

    public void onBeforeInnerCall(String methodName, Object[] args) throws Throwable {
        String[] ts = methodName.split("#");
        System.out.println(
                "Before Inner Call: " + ts[0] + " # " + InvokeDescriptorUtils.descToText(ts[1], true) +
                Arrays.toString(
                        args == null ? new Object[0] : args
                )
        );
    }

    public void onAfterInnerCall(Object returnValue) throws Throwable {
        System.out.println(
                "After Inner Call: " + returnValue
        );
    }
}
