package agent.server.transform.impl.dynamic;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.jvmti.JvmtiUtils;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.AdditionalTransformEvent;
import agent.server.transform.TransformMgr;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.impl.utils.AgentClassPool;
import agent.server.transform.impl.utils.ClassPoolUtils;
import agent.server.transform.impl.utils.MethodFinder;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicClassTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "$sys_dynamic";
    public static final String KEY_CONFIG = "config";
    private static final Logger logger = Logger.getLogger(DynamicClassTransformer.class);
    private static final String METHOD_AFTER = "After";
    private static final String skipPackage = "agent.";

    private static final String methodInfoClassName = MethodInfo.class.getName();
    private static final String methodInfoVar = "methodInfo";
    private static final int defaultMaxLevel = 50;

    private DynamicConfigItem item;
    private String key;
    private int maxLevel;
    private Set<String> transformedMethods = new HashSet<>();
    private Set<String> additionalClassNames = new HashSet<>();

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        item = (DynamicConfigItem) config.get(KEY_CONFIG);
        if (item == null)
            throw new RuntimeException("No config item found.");
        key = Utils.sUuid();
        maxLevel = Math.min(Math.max(item.maxLevel, 1), defaultMaxLevel);
    }

    @Override
    protected void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {
        DynamicRuleRegistry.getInstance().regRuleInvokeIfAbsent(key, k -> new RuleInvokeItem(item));
        final int level = 0;
        MethodInfo methodInfo = newMethodInfo(ctMethod, level);
        switch (item.position) {
            case BEFORE:
            case AFTER:
            case WRAP:
                processMethodCode(ctMethod, methodInfo);
                break;
            case BEFORE_MC:
            case AFTER_MC:
            case WRAP_MC:
                ctMethod.instrument(new NestedExprEditor(level));
                break;
            default:
                throw new RuntimeException("Unknown position: " + item.position);
        }
    }

    @Override
    protected void postTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                 ProtectionDomain protectionDomain, byte[] classfileBuffer, byte[] newBuffer) throws Exception {
        additionalClassNames.remove(TransformerInfo.getClassName(className));
        if (!additionalClassNames.isEmpty()) {
            additionalClassNames.forEach(additionalClassName -> logger.debug("Additional class: {}", additionalClassName));

            Map<String, byte[]> classNameToBytes = new HashMap<>();
            for (String additionalClassName : additionalClassNames) {
                classNameToBytes.put(additionalClassName, AgentClassPool.getInstance().get(additionalClassName).toBytecode());
            }
            EventListenerMgr.fireEvent(new AdditionalTransformEvent(item.context, classNameToBytes));
        }
    }

    private void processMethodCode(CtMethod ctMethod, MethodInfo methodInfo) throws Exception {
        String methodLongName = methodInfo.toString();
        if (transformedMethods.contains(methodLongName)) {
            logger.debug("{} has been transformed.", methodLongName);
            return;
        }
        String preCode = "";
        if (item.needMethodInfo) {
            ctMethod.addLocalVariable(methodInfoVar, AgentClassPool.getInstance().get(methodInfoClassName));
            preCode = methodInfoVar + " = " + methodInfo.newCode();
        }
        switch (item.position) {
            case BEFORE:
            case BEFORE_MC:
                ctMethod.insertBefore(newCode(preCode, "invokeBefore"));
                break;
            case AFTER:
            case AFTER_MC:
                ctMethod.insertAfter(newCode(preCode, "invokeAfter"));
                break;
            case WRAP:
            case WRAP_MC:
                ctMethod.insertBefore(newCode(preCode, "invokeWrapBefore"));
                ctMethod.insertAfter(newCode("", "invokeWrapAfter"));
                break;
        }
        transformedMethods.add(methodLongName);
        additionalClassNames.add(methodInfo.className);
    }

    private String newCode(String preCode, String method) {
        StringBuilder sb = new StringBuilder(preCode);

        sb.append(RuleInvokeMgr.class.getName()).append(".").append(method)
                .append("(\"").append(key).append("\"");

        if (item.needMethodInfo)
            sb.append(", ").append(methodInfoVar);
        else
            sb.append(", null");

        sb.append(", $args");
        if (method.contains(METHOD_AFTER))
            sb.append(", ($w) $_");

        sb.append(");");
        String code = sb.toString();
        logger.debug("Position: {}, code: {}", item.position, code);
        return code;
    }

    private class NestedExprEditor extends ExprEditor {
        private final int level;

        NestedExprEditor(int level) {
            this.level = level;
        }

        @Override
        public void edit(MethodCall mc) {
            Utils.wrapToRtError(() -> {
                CtMethod ctMethod = mc.getMethod();
                MethodInfo methodInfo = newMethodInfo(ctMethod, level);
                if (Modifier.isAbstract(ctMethod.getModifiers())) {
                    logger.debug("Method is abstract: {}", ctMethod.getLongName());
                    Collection<CtMethod> implMethods = findImplMethods(ctMethod.getDeclaringClass(), methodInfo);
                    implMethods.forEach(implMethod -> logger.debug("Find impl method: {}", implMethod.getLongName()));
                    for (CtMethod implMethod : implMethods) {
                        if (!skipMethod(implMethod))
                            processMethod(implMethod, newMethodInfo(implMethod, level));
                    }
                } else if (!skipMethod(ctMethod)) {
                    logger.debug("Method is concrete: {}", ctMethod.getLongName());
                    processMethod(ctMethod, methodInfo);
                }
            });
        }

        private Collection<String> findImplClassNames(MethodInfo methodInfo) throws Exception {
            MethodRuleFilter.FindImplClassPolicy policy = item.methodRuleFilter.getFindImplClassPolicy();
            switch (policy) {
                case FROM_LOADED_CLASSES:
                    return Utils.wrapToRtError(
                            () -> {
                                List<Class<?>> subClassList = JvmtiUtils.getInstance()
                                        .findLoadedSubTypes(
                                                TransformMgr.getInstance()
                                                        .getClassFinder()
                                                        .findClass(item.context, methodInfo.className)
                                        );
                                ClassPoolUtils.getClassPathRecorder().add(subClassList);

                                return subClassList.stream()
                                        .map(Class::getName)
                                        .collect(Collectors.toList());
                            },
                            () -> "Get impl classes failed: " + methodInfo
                    );
                case USER_DEFINED:
                    return item.methodRuleFilter.getImplClasses(methodInfo);
                default:
                    throw new RuntimeException("Invalid policy: " + policy);
            }
        }

        private Collection<CtMethod> findImplMethods(CtClass baseClass, MethodInfo methodInfo) {
            return Utils.wrapToRtError(() -> {
                        Collection<String> implClassNames = findImplClassNames(methodInfo);
                        logger.debug("Find class {} impl classes: {}", methodInfo.className, implClassNames);
                        if (implClassNames == null || implClassNames.isEmpty()) {
                            return Collections.emptyList();
                        } else {
                            Set<String> implClassSet = new HashSet<>(implClassNames);
                            if (implClassSet.isEmpty())
                                return Collections.emptyList();
                            CtClass[] implClasses = AgentClassPool.getInstance().get(
                                    implClassSet.toArray(new String[0])
                            );
                            for (CtClass implClass : implClasses) {
                                if (!implClass.subtypeOf(baseClass))
                                    throw new RuntimeException("Class " + implClass.getName() + " is not sub type of " + baseClass.getName());
                            }
                            return Stream.of(implClasses)
                                    .map(ctClass -> findMethod(ctClass, methodInfo))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                        }
                    },
                    () -> "Find impl methods failed: " + methodInfo.toString()
            );
        }

        private CtMethod findMethod(CtClass ctClass, MethodInfo methodInfo) {
            try {
                CtMethod rsMethod = Stream.of(ctClass.getDeclaredMethods())
                        .filter(ctMethod -> ctMethod.getSignature().equals(methodInfo.signature))
                        .findAny()
                        .orElse(null);
                if (rsMethod == null)
                    logger.warn("No method find by class: {}, method name: {}, method desc: {}",
                            ctClass.getName(), methodInfo.methodName, methodInfo.signature);
                return rsMethod;
            } catch (Exception e) {
                logger.error("Find method failed, class: {}, method name: {}, method desc: {}", e,
                        ctClass.getName(), methodInfo.methodName, methodInfo.signature);
                return null;
            }
        }

        private void processMethod(CtMethod ctMethod, MethodInfo methodInfo) {
            Utils.wrapToRtError(() -> {
                        logger.debug("Process method: {}", ctMethod.getLongName());
                        int nextLevel = level + 1;
                        if (nextLevel < maxLevel &&
                                (item.methodRuleFilter == null ||
                                        item.methodRuleFilter.stepInto(methodInfo))
                                ) {
                            logger.debug("stepInto: {}", methodInfo);
                            ctMethod.instrument(new NestedExprEditor(nextLevel));
                        }
                        if (item.methodRuleFilter.accept(methodInfo)) {
                            logger.debug("accept: {}", methodInfo);
                            processMethodCode(ctMethod, methodInfo);
                        }
                    },
                    () -> "Process method failed: " + ctMethod.getLongName()
            );
        }
    }

    private boolean skipMethod(CtMethod ctMethod) {
        String declaredClassName = ctMethod.getDeclaringClass().getName();
        boolean skip = MethodFinder.isMethodEmpty(ctMethod) ||
                declaredClassName.startsWith(skipPackage);
        if (skip)
            logger.debug("Skip method: {}", ctMethod.getLongName());
        return skip;
    }

    private MethodInfo newMethodInfo(CtMethod ctMethod, int level) {
        CtClass ctClass = ctMethod.getDeclaringClass();
        return new MethodInfo(
                ctClass.getName(),
                ctMethod.getName(),
                ctMethod.getSignature(),
                level
        );
    }
}
