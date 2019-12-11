package agent.server.transform.tools.asm;

import agent.base.utils.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyTransformMgr {
    private static final Logger logger = Logger.getLogger(ProxyTransformMgr.class);
    private static final ProxyTransformMgr instance = new ProxyTransformMgr();

    private AtomicInteger idGenerator = new AtomicInteger(0);
    private Map<Method, Integer> methodToId = new ConcurrentHashMap<>();
    private Map<Integer, ProxyCallConfig> idToCallConfig = new ConcurrentHashMap<>();
    private Map<Integer, ProxyCallSite> idToCallSite = new ConcurrentHashMap<>();

    public static ProxyTransformMgr getInstance() {
        return instance;
    }

    private ProxyTransformMgr() {
    }

    public ProxyResult transform(byte[] classData, Collection<ProxyRegInfo> regInfos) {
        Map<Method, List<ProxyRegInfo>> methodToRegInfos = new HashMap<>();
        Map<Integer, Method> newIdToMethod = new HashMap<>();
        regInfos.forEach(
                regInfo -> {
                    Method destMethod = regInfo.getDestMethod();
                    methodToId.computeIfAbsent(
                            regInfo.getDestMethod(),
                            key -> {
                                int newId = idGenerator.incrementAndGet();
                                newIdToMethod.put(newId, destMethod);
                                return newId;
                            }
                    );
                    methodToRegInfos.computeIfAbsent(
                            destMethod,
                            key -> new ArrayList<>()
                    ).add(regInfo);
                }
        );

        byte[] newClassData = null;
        if (!newIdToMethod.isEmpty()) {
            newClassData = NewAsmTransformProxy.transform(
                    classData,
                    newIdToMethod
            );
            AsmUtils.print(newClassData);
            String verifyResult = AsmUtils.getVerifyResult(newClassData);
            if (!verifyResult.isEmpty()) {
                logger.error("Verify transform failed: \n{}", verifyResult);
                throw new RuntimeException("Verify transform failed.");
            }
        }

        return new ProxyResult(
                newClassData,
                methodToRegInfos
        );
    }

    public void reg(ProxyResult result) {
        result.methodToRegInfos.forEach(
                (method, regInfos) -> {
                    if (!regInfos.isEmpty()) {
                        int methodId = Optional.ofNullable(
                                methodToId.get(method)
                        ).orElseThrow(
                                () -> new RuntimeException("No id found for method: " + method)
                        );
                        ProxyCallConfig callConfig = idToCallConfig.computeIfAbsent(
                                methodId,
                                key -> new ProxyCallConfig(method)
                        );
                        regInfos.forEach(
                                regInfo -> regInfo.getPosToCalInfos()
                                        .forEach(callConfig::reg)
                        );
                    }
                }
        );
    }

    public Object onDelegate(Object target, Class<?> targetClass, String targetMethodName, int methodId, Object[] args) throws Throwable {
        ProxyCallConfig callConfig = Optional.ofNullable(
                idToCallConfig.get(methodId)
        ).orElseThrow(
                () -> new RuntimeException("No call config found by method id: " + methodId)
        );
        return idToCallSite.computeIfAbsent(
                methodId,
                key -> new ProxyCallSite(
                        targetClass,
                        targetMethodName
                )
        ).invoke(callConfig, target, args);
    }

    public static class ProxyResult {
        private final byte[] classData;
        private final Map<Method, List<ProxyRegInfo>> methodToRegInfos;

        private ProxyResult(byte[] classData, Map<Method, List<ProxyRegInfo>> methodToRegInfos) {
            this.classData = classData;
            this.methodToRegInfos = methodToRegInfos;
        }

        public byte[] getClassData() {
            return classData;
        }
    }
}
