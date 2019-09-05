package agent.server.transform.impl.dynamic;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.server.transform.impl.AbstractConfigTransformer;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Map;

import static agent.server.transform.config.rule.MethodRule.Position.*;

public class DynamicClassTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "$sys_dynamic";
    public static final String KEY_CONFIG = "config";
    private static final Logger logger = Logger.getLogger(DynamicClassTransformer.class);
    private static final String METHOD_BEFORE = "Before";

    private DynamicConfigItem item;
    private String key;

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
    }

    @Override
    protected void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {
        MethodInfo methodInfo = DynamicRuleRegistry.getInstance().regMethodInfoIfAbsent(
                ctMethod.getLongName(),
                k -> newMethodInfo(ctMethod)
        );
        DynamicRuleRegistry.getInstance().regRuleInvokeIfAbsent(key,
                k -> new RuleInvokeItem(item, methodInfo)
        );
        switch (item.position) {
            case BEFORE:
                ctMethod.insertBefore(newCode("invokeBefore"));
                break;
            case AFTER:
                ctMethod.insertAfter(newCode("invokeAfter"));
                break;
            case WRAP:
                ctMethod.insertBefore(newCode("invokeWrapBefore"));
                ctMethod.insertAfter(newCode("invokeWrapAfter"));
                break;
            case BEFORE_MC:
            case AFTER_MC:
            case WRAP_MC:
                replaceCode(ctMethod, methodInfo);
                break;
            default:
                throw new RuntimeException("Unknown position: " + item.position);
        }
    }

    private void replaceCode(CtMethod ctMethod, MethodInfo methodInfo) throws Exception {
        ctMethod.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall mc) throws CannotCompileException {
                final String mcKey = getMethodCallKey(mc);
                MethodCallInfo mcInfo = methodInfo.regIfAbsent(
                        mcKey,
                        k -> newMethodCallInfo(mc)
                );
                if (item.methodCallFilter == null || item.methodCallFilter.accept(mcInfo)) {
                    mc.replace(newMcCode(mcKey));
                }
            }
        });
    }

    private String newMcCode(String mcKey) {
        StringBuilder sb = new StringBuilder();
        if (item.position == BEFORE_MC || item.position == WRAP_MC)
            sb.append(newCode("invokeBeforeMC", mcKey));
        sb.append("$_ = $proceed($$);");
        if (item.position == AFTER_MC || item.position == WRAP_MC)
            sb.append(newCode("invokeAfterMC", mcKey));
        return sb.toString();
    }

    private String newCode(String method) {
        return newCode(method, null);
    }

    private String newCode(String method, String mcKey) {
        StringBuilder sb = new StringBuilder();
        sb.append(RuleInvokeMgr.class.getName()).append(".").append(method).append("(\"").append(key).append("\"");
        if (item.needMethodCallInfo)
            sb.append(", \"").append(mcKey).append("\"");
        sb.append(", $args");
        if (method.contains(METHOD_BEFORE))
            sb.append(", ($w) $_");
        sb.append(");");
        String code = sb.toString();
        logger.debug("Position: {}, code: {}", item.position, code);
        return code;
    }

    private MethodInfo newMethodInfo(CtMethod ctMethod) {
        return new MethodInfo(
                ctMethod.getDeclaringClass().getName(),
                ctMethod.getName(),
                ctMethod.getSignature()
        );
    }

    private String getMethodCallKey(MethodCall mc) {
        return mc.getClassName() + "."
                + mc.getMethodName() + Descriptor.toString(mc.getSignature());
    }

    private MethodCallInfo newMethodCallInfo(MethodCall mc) {
        return new MethodCallInfo(
                mc.getClassName(),
                mc.getMethodName(),
                mc.getSignature()
        );
    }

}
