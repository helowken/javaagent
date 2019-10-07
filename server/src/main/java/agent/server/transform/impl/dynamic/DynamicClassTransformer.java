package agent.server.transform.impl.dynamic;

import agent.base.plugin.PluginFactory;
import agent.base.utils.Logger;
import agent.base.utils.MethodSignatureUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.hook.plugin.ClassFinder;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.AdditionalTransformEvent;
import agent.server.transform.BytecodeMethodFinder;
import agent.server.transform.ClassDataFinder;
import agent.server.transform.TransformMgr;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.impl.utils.AgentClassPool;
import agent.server.transform.impl.utils.ClassPathRecorder;
import agent.server.transform.impl.utils.ClassPoolUtils;
import agent.server.transform.impl.utils.MethodFinder;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicClassTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "$sys_dynamic";
    public static final String KEY_CONFIG = "config";
    private static final Logger logger = Logger.getLogger(DynamicClassTransformer.class);
    private static final String METHOD_AFTER = "After";

    private static final String methodInfoClassName = MethodInfo.class.getName();
    private static final String methodInfoVar = "methodInfo";
    private static final int defaultMaxLevel = 50;
    private static final AgentClassPool classPool = AgentClassPool.getInstance();

    private DynamicConfigItem item;
    private String key;
    private int maxLevel;
    private Set<String> transformedMethods = new HashSet<>();
    private Set<String> additionalClassNames = new HashSet<>();
    private Map<String, Map<String, Class<?>>> baseToSubClassMap = new ConcurrentHashMap<>();
    private List<BytecodeMethodFinder> bytecodeMethodFinders;

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
        maxLevel = Math.min(Math.max(item.maxLevel, 10), defaultMaxLevel);
    }

    @Override
    protected void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {
        DynamicRuleRegistry.getInstance().regRuleInvokeIfAbsent(key, k -> new RuleInvokeItem(item));
        final int level = 0;
        switch (item.position) {
            case BEFORE:
            case AFTER:
            case WRAP:
                handleMethod(ctMethod, level, false);
                break;
            case BEFORE_MC:
            case AFTER_MC:
            case WRAP_MC:
                if (needToBeTransformed(ctMethod)) {
                    MethodInfo methodInfo = newMethodInfo(ctMethod, level);
                    if (!Modifier.isAbstract(methodInfo.methodModifiers))
                        ctMethod.instrument(new NestedExprEditor(level));
                    for (CtMethod overrideMethod : findOverrideMethods(methodInfo)) {
                        overrideMethod.instrument(new NestedExprEditor(level));
                    }
                    for (CtMethod bytecodeMethod : findBytecodeMethods(methodInfo)) {
                        bytecodeMethod.instrument(new NestedExprEditor(level));
                    }
                }
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
                classNameToBytes.put(
                        additionalClassName,
                        classPool.get(additionalClassName).toBytecode()
                );
            }
            EventListenerMgr.fireEvent(
                    new AdditionalTransformEvent(item.context, classNameToBytes)
            );
        }
    }

    private boolean needToBeTransformed(CtMethod ctMethod) {
        return !transformedMethods.contains(
                ctMethod.getLongName()
        ) &&
                !Modifier.isNative(
                        ctMethod.getModifiers()
                ) &&
                !ClassPathRecorder.isNativePackage(
                        ctMethod.getDeclaringClass().getName()
                );
    }

    private void addToTransformed(CtMethod ctMethod) {
        transformedMethods.add(ctMethod.getLongName());
    }

    private void processMethodCode(CtMethod ctMethod, MethodInfo methodInfo) {
        Utils.wrapToRtError(() -> {
            logger.debug("accept: {}", methodInfo);
            String preCode = "";
            if (item.needMethodInfo) {
                ctMethod.addLocalVariable(methodInfoVar, classPool.get(methodInfoClassName));
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
            additionalClassNames.add(methodInfo.className);
        });
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
        return sb.toString();
//        logger.debug("Position: {}, code: {}", item.position, code);
    }

    private class NestedExprEditor extends ExprEditor {
        private final int level;

        NestedExprEditor(int level) {
            this.level = level;
        }

        @Override
        public void edit(MethodCall mc) {
            Utils.wrapToRtError(
                    () -> handleMethod(mc.getMethod(), level, true)
            );
        }
    }

    private void handleMethod(CtMethod ctMethod, int level, boolean stepInto) {
        if (needToBeTransformed(ctMethod)) {
            ctMethod.getDeclaringClass().defrost();

            MethodInfo methodInfo = newMethodInfo(ctMethod, level);
            if (Modifier.isAbstract(methodInfo.methodModifiers)) {
                logger.debug("Method is abstract: {}", ctMethod.getLongName());
                addToTransformed(ctMethod);
            } else
                processMethod(ctMethod, methodInfo, level, stepInto);

            processMethods(
                    findOverrideMethods(methodInfo),
                    level,
                    stepInto
            );

            processMethods(
                    findBytecodeMethods(methodInfo),
                    level,
                    stepInto
            );
        }
    }

    private Collection<CtMethod> findOverrideMethods(MethodInfo methodInfo) {
        boolean canBeOverridden = ReflectionUtils.canBeOverridden(
                methodInfo.classModifiers,
                methodInfo.methodModifiers
        );
        if (canBeOverridden && item.methodRuleFilter.needGetOverrideMethods(methodInfo))
            return Utils.wrapToRtError(() -> {
//                        logger.debug("Find impl classes of class {}", methodInfo.className);
                        Collection<String> implClassNames = findSubClassNames(methodInfo);
//                    logger.debug("Found {} impl classes: {}", methodInfo, implClassNames);
                        if (implClassNames == null || implClassNames.isEmpty())
                            return Collections.emptyList();
                        else {
                            CtClass baseClass = classPool.get(methodInfo.className);
                            Collection<CtClass> implClasses = new LinkedList<>();
                            for (String implClassName : implClassNames) {
                                CtClass implClass = null;
                                try {
                                    implClass = classPool.get(implClassName);
                                } catch (Exception e) {
                                    logger.error("Find impl class failed: {}", e, implClassName);
                                    InvalidClassNameCache.getInstance().add(item.context, implClassName);
                                }
                                if (implClass != null) {
                                    if (!implClass.subtypeOf(baseClass))
                                        throw new RuntimeException("Class " + implClass.getName() + " is not sub type of " + baseClass.getName());
                                    implClasses.add(implClass);
                                }
                            }
                            Set<String> foundMethods = new HashSet<>();
                            return implClasses.stream()
                                    .map(ctClass -> findCtMethod(ctClass, methodInfo, foundMethods))
                                    .filter(Objects::nonNull)
//                                .peek(m -> logger.debug("Find override method: {}", m.getLongName()))
                                    .collect(Collectors.toList());
                        }
                    },
                    () -> "Find impl methods failed: " + methodInfo.toString()
            );
        return Collections.emptyList();
    }

    private Collection<String> findSubClassNames(MethodInfo methodInfo) {
        return Utils.wrapToRtError(
                () -> Collections.unmodifiableCollection(
                        baseToSubClassMap.computeIfAbsent(
                                methodInfo.className,
                                baseClassName -> {
                                    Map<String, Class<?>> subClassMap = new HashMap<>(
                                            item.methodRuleFilter.getImplClasses(
                                                    methodInfo,
                                                    () -> ClassCache.getInstance().getSubClassMap(item.context, baseClassName)
                                            )
                                    );
                                    ClassFinder classFinder = getClassFinder();
                                    subClassMap.entrySet().removeIf(
                                            entry -> {
                                                String subClassName = entry.getKey();
                                                if (InvalidClassNameCache.getInstance().contains(item.context, subClassName))
                                                    return true;
                                                if (entry.getValue() == null) {
                                                    try {
                                                        entry.setValue(
                                                                classFinder.findClass(item.context, subClassName)
                                                        );
                                                    } catch (Exception e) {
                                                        logger.error("Find class failed, context: {}, class: {}", item.context, subClassName);
                                                        InvalidClassNameCache.getInstance().add(item.context, subClassName);
                                                        return true;
                                                    }
                                                }
                                                return false;
                                            }
                                    );

                                    ClassPoolUtils.getClassPathRecorder().add(
                                            new LinkedList<>(subClassMap.values()),
                                            (clazz, error) -> {
                                                logger.error("Find ref class failed: {}", error, clazz.getName());
                                                String invalidClassName = clazz.getName();
                                                InvalidClassNameCache.getInstance().add(item.context, invalidClassName);
                                                subClassMap.remove(invalidClassName);

                                                byte[] classData = ClassDataFinder.getInstance().getClassData(clazz);
                                                if (classData != null)
                                                    logger.debug("Found class data for failed class: {}", clazz.getName());
                                                else
                                                    logger.debug("No class data found for failed class: {}", clazz.getName());
                                            }
                                    );
                                    return subClassMap;
                                }
                        ).keySet()
                ),
                () -> "Get impl classes failed: " + methodInfo
        );
    }

    private CtMethod findCtMethod(CtClass ctClass, MethodInfo methodInfo, Set<String> foundMethods) {
        try {
            CtMethod rsMethod = findCtMethodHelper(ctClass, methodInfo.methodName, methodInfo.signature);
            if (rsMethod == null)
                logger.warn("No method find by class: {}, methodInfo: {}", ctClass.getName(), methodInfo);
            else {
                String longName = rsMethod.getLongName();
                if (foundMethods.contains(longName))
                    rsMethod = null;
                else
                    foundMethods.add(longName);
            }
            return rsMethod;
        } catch (Exception e) {
            logger.error("Find method failed, class: {}, method name: {}, method desc: {}", e,
                    ctClass.getName(), methodInfo.methodName, methodInfo.signature);
            return null;
        }
    }

    private void processMethods(Collection<CtMethod> ctMethods, int level, boolean stepInto) {
        ctMethods.forEach(
                ctMethod -> processMethod(
                        ctMethod,
                        newMethodInfo(ctMethod, level),
                        level,
                        stepInto
                )
        );
    }

    private void processMethod(CtMethod ctMethod, MethodInfo methodInfo, int level, boolean stepInto) {
//            logger.debug("Process method: {}", ctMethod.getLongName());
        if (!needToBeTransformed(ctMethod) || skipMethod(ctMethod))
            return;
        addToTransformed(ctMethod);
        if (stepInto)
            Utils.wrapToRtError(() -> {
                        int nextLevel = level + 1;
                        if (nextLevel < maxLevel) {
                            if (item.methodRuleFilter.stepInto(methodInfo)) {
                                logger.debug("stepInto: {}", methodInfo);
                                ctMethod.instrument(new NestedExprEditor(nextLevel));
                            }
                        } else
                            logger.warn("Reach the max nested level: {}, methodInfo: {}", maxLevel, methodInfo);
                        if (item.methodRuleFilter.accept(methodInfo))
                            processMethodCode(ctMethod, methodInfo);
                    },
                    () -> "Process method failed: " + ctMethod.getLongName()
            );
        else {
//            logger.debug("Not to step into: {}", methodInfo);
            processMethodCode(ctMethod, methodInfo);
        }
    }

    private Collection<CtMethod> findBytecodeMethods(MethodInfo methodInfo) {
        if (!item.methodRuleFilter.needGetBytecodeMethods(methodInfo))
            return Collections.emptyList();
        if (bytecodeMethodFinders == null)
            bytecodeMethodFinders = PluginFactory.getInstance().findAll(BytecodeMethodFinder.class);
//        logger.debug("Found bytecode method finders: {}", bytecodeMethodFinders);
        Set<Method> bytecodeMethods = new HashSet<>();
        bytecodeMethodFinders.forEach(
                methodFinder -> bytecodeMethods.addAll(
                        methodFinder.findBytecodeMethods(
                                methodInfo,
                                getClassFinder().findClassLoader(item.context),
                                this::methodGetter
                        )
                )
        );
        return bytecodeMethods.isEmpty() ?
                Collections.emptySet() :
                bytecodeMethods.stream()
                        .map(
                                method -> Optional.ofNullable(
                                        findCtMethodHelper(
                                                classPool.get(
                                                        method.getDeclaringClass().getName()
                                                ),
                                                method.getName(),
                                                MethodSignatureUtils.getSignature(method)
                                        )
                                ).orElseThrow(
                                        () -> new RuntimeException("No ct method found for method: " + method)
                                )
                        )
                        .collect(Collectors.toSet());
    }

    private CtMethod findCtMethodHelper(CtClass ctClass, String methodName, String signature) {
        return Stream.of(ctClass.getMethods())
                .filter(ctMethod -> !Modifier.isAbstract(ctMethod.getModifiers()))
                .filter(ctMethod -> isMethodMatches(ctMethod, methodName, signature))
                .findAny()
                .orElse(null);
    }

    private Method methodGetter(MethodInfo targetMethodInfo) {
        return Utils.wrapToRtError(() ->
                Optional.ofNullable(
                        ReflectionUtils.findFirstMethod(
                                getClassFinder().findClass(
                                        item.context,
                                        targetMethodInfo.className
                                ),
                                targetMethodInfo.methodName,
                                targetMethodInfo.signature
                        )
                ).orElseThrow(
                        () -> new RuntimeException("No method found by methodInfo: " + targetMethodInfo)
                )
        );
    }

    private boolean skipMethod(CtMethod ctMethod) {
        String declaredClassName = ctMethod.getDeclaringClass().getName();
        return MethodFinder.isMethodEmpty(ctMethod) ||
                ClassPathRecorder.isNativePackage(declaredClassName);
//        if (skip)
//            logger.debug("Skip method: {}", ctMethod.getLongName());
    }

    private MethodInfo newMethodInfo(CtMethod ctMethod, int level) {
        CtClass ctClass = ctMethod.getDeclaringClass();
        return new MethodInfo(
                ctClass.getName(),
                ctMethod.getName(),
                ctMethod.getSignature(),
                ctClass.getModifiers(),
                ctMethod.getModifiers(),
                level
        );
    }

    private boolean isMethodMatches(CtMethod ctMethod, String methodName, String signature) {
        return ctMethod.getName().equals(methodName) &&
                ctMethod.getSignature().equals(signature);
    }

    private ClassFinder getClassFinder() {
        return TransformMgr.getInstance().getClassFinder();
    }

}
