package agent.server.transform.impl.dynamic;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.utils.AgentClassPool;
import agent.server.transform.impl.utils.MethodFinder;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Map;

public class DynamicClassTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "$sys_dynamic";
    public static final String KEY_CONFIG = "config";
    private static final Logger logger = Logger.getLogger(DynamicClassTransformer.class);
    private static final String METHOD_AFTER = "After";

    private static final String methodInfoClassName = MethodInfo.class.getName();
    private static final String methodInfoVar = "methodInfo";
    private static final int defaultMaxLevel = 10;

    private DynamicConfigItem item;
    private String key;
    private int maxLevel;

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

    private void processMethodCode(CtMethod ctMethod, MethodInfo methodInfo) throws Exception {
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

    public class NestedExprEditor extends ExprEditor {
        private final int level;

        NestedExprEditor(int level) {
            this.level = level;
        }

        @Override
        public void edit(MethodCall mc) {
            try {
                CtMethod method = mc.getMethod();
                if (!MethodFinder.isMethodEmpty(method)) {
                    MethodInfo methodInfo = newMethodInfo(method, level);
                    int nextLevel = level + 1;
                    if (nextLevel < maxLevel &&
                            (item.methodCallFilter == null ||
                                    item.methodCallFilter.accept(methodInfo))
                            ) {
                        method.instrument(new NestedExprEditor(nextLevel));
                        processMethodCode(mc.getMethod(), methodInfo);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private MethodInfo newMethodInfo(CtMethod ctMethod, int level) {
        return new MethodInfo(
                ctMethod.getDeclaringClass().getName(),
                ctMethod.getName(),
                ctMethod.getSignature(),
                level
        );
    }
}
