package agent.server.transform.tools.asm;

import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.Logger;
import agent.bootstrap.ProxyDelegate;
import agent.bootstrap.ProxyIntf;
import agent.invoke.DestInvoke;
import agent.invoke.proxy.ProxyCallSite;
import agent.invoke.proxy.ProxyItem;
import agent.invoke.proxy.ProxyRegInfo;
import agent.invoke.proxy.ProxyResult;
import agent.server.ServerListener;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.revision.ClassDataRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyTransformMgr implements ProxyIntf, ServerListener {
    private static final Logger logger = Logger.getLogger(ProxyTransformMgr.class);
    private static final ProxyTransformMgr instance = new ProxyTransformMgr();
    private final Map<Integer, ProxyCallSite> idToCallSite = new ConcurrentHashMap<>();
    private final ProxyController controller = new ProxyController();

    public static ProxyTransformMgr getInstance() {
        return instance;
    }

    private ProxyTransformMgr() {
    }

    public Map<String, List<String>> getCallSiteDisplay(Integer invokeId) {
        return getCallSite(invokeId).getPosToDisplayStrings();
    }

    public void reset(Collection<Class<?>> classes) {
        DestInvokeIdRegistry.getInstance().run(
                classToInvokeToId -> {
                    classes.forEach(
                            clazz -> {
                                Map<DestInvoke, Integer> invokeToId = classToInvokeToId.remove(clazz);
                                if (invokeToId != null)
                                    invokeToId.values().forEach(idToCallSite::remove);
                                ClassDataRepository.getInstance().removeClassData(clazz);
                            }
                    );
                    return null;
                }
        );
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

    private void verifyClassData(byte[] newClassData) {
//        String verifyResult = null;
//        try {
//            verifyResult = AsmUtils.getVerifyResult(
//                    targetClass.getClassLoader(),
//                    newClassData,
//                    false
//            );
//        } catch (Exception e) {
//            logger.error("Get verify result fail.", e);
//        }
//        if (verifyResult != null && !verifyResult.isEmpty()) {
//            logger.error("Verify {} transform failed: \n{}\n=================\n{}",
//                    targetClass.getName(),
//                    AsmUtils.getVerifyResult(
//                            targetClass.getClassLoader(),
//                            newClassData,
//                            true
//                    ),
//                    AsmUtils.convertToReadableContent(newClassData)
//            );
//            return new ProxyResult(
//                    item,
//                    new RuntimeException("Verify transform failed.")
//            );
//        }
    }

    private ProxyResult doTransform(ProxyItem item, Function<Class<?>, byte[]> classDataFunc) {
        Class<?> targetClass = item.getTargetClass();
        try {
            byte[] newClassData = AsmUtils.transform(
                    targetClass,
                    classDataFunc.apply(targetClass),
                    item.getIdToInvoke()
            );
            verifyClassData(newClassData);
            ClassDataRepository.getInstance().saveClassData(targetClass, newClassData);
            return new ProxyResult(item);
        } catch (Exception e) {
            logger.error("doTransform failed: {}", e, targetClass.getName());
            return new ProxyResult(item, e);
        }
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

    public void onBefore(int invokeId, Object instanceOrNull, Object[] args) {
        controller.onBefore(
                invokeId,
                () -> getCallSite(invokeId).invokeBefore(instanceOrNull, args)
        );
    }

    public void onReturning(int invokeId, Object instanceOrNull, Object returnValue) {
        controller.onReturning(
                invokeId,
                () -> getCallSite(invokeId).invokeOnReturning(instanceOrNull, returnValue)
        );
    }

    public void onThrowing(int invokeId, Object instanceOrNull, Throwable error) {
        controller.onThrowing(
                invokeId,
                () -> getCallSite(invokeId).invokeOnThrowing(instanceOrNull, error)
        );
    }

    public void onCatching(int invokeId, Object instanceOrNull, Throwable error) {
        controller.onCatching(
                invokeId,
                () -> getCallSite(invokeId).invokeOnCatching(instanceOrNull, error)
        );
    }

    public void onBeforeInnerCall(long callNum, String methodName, Object[] args) {
        String[] ts = methodName.split("#");
        System.out.println(
                "Before Inner Call " + callNum + ": " + ts[0] + " # " + InvokeDescriptorUtils.descToText(ts[1]) +
                        Arrays.toString(
                                args == null ? new Object[0] : args
                        )
        );
    }

    public void onAfterInnerCall(long callNum, Object returnValue) {
        System.out.println(
                "After Inner Call " + callNum + ": " + returnValue
        );
    }

    @Override
    public void onStartup(Object[] args) {
        ProxyDelegate.getInstance().setProxy(this);
    }

    @Override
    public void onShutdown() {
    }

    private static class ProxyController {
        private final ThreadLocal<Set<Integer>> before = new ThreadLocal<>();
        private final ThreadLocal<Set<Integer>> throwing = new ThreadLocal<>();
        private final ThreadLocal<Set<Integer>> catching = new ThreadLocal<>();
        private final ThreadLocal<Set<Integer>> returning = new ThreadLocal<>();

        void onBefore(int invokeId, Runnable runnable) {
            run(before, invokeId, runnable);
        }

        void onReturning(int invokeId, Runnable runnable) {
            run(returning, invokeId, runnable);
        }

        void onThrowing(int invokeId, Runnable runnable) {
            run(throwing, invokeId, runnable);
        }

        void onCatching(int invokeId, Runnable runnable) {
            run(catching, invokeId, runnable);
        }

        private void run(ThreadLocal<Set<Integer>> local, int invokeId, Runnable runnable) {
            if (mark(local, invokeId)) {
                runnable.run();
                remove(local, invokeId);
            }
        }

        private boolean mark(ThreadLocal<Set<Integer>> local, int invokeId) {
            Set<Integer> vs = local.get();
            if (vs == null) {
                vs = new HashSet<>();
                local.set(vs);
            }
            return vs.add(invokeId);
        }

        private void remove(ThreadLocal<Set<Integer>> local, int invokeId) {
            Set<Integer> vs = local.get();
            vs.remove(invokeId);
            if (vs.isEmpty())
                local.remove();
        }
    }
}
