package agent.server.transform.impl.dynamic;

import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.List;


public class RuleInvokeMgr {
    private static final Logger logger = Logger.getLogger(RuleInvokeMgr.class);


    public static void invokeBefore(String key, Object[] args) {
        invoke(key, null, args, null, null);
    }

    public static void invokeAfter(String key, Object[] args, Object returnValue) {
        invoke(key, null, args, returnValue, null);
    }

    public static void invokeWrapBefore(String key, Object[] args) {
        invoke(key, null, args, null, true);
    }

    public static void invokeWrapAfter(String key, Object[] args, Object returnValue) {
        invoke(key, null, args, returnValue, false);
    }

    public static void invokeBeforeMC(String key, String mcKey, Object[] args) {
        invoke(key, mcKey, args, null, true);
    }

    public static void invokeAfterMC(String key, String mcKey, Object[] args, Object returnValue) {
        invoke(key, mcKey, args, returnValue, false);
    }

    public static void invokeWrapBeforeMC(String key, String mcKey, Object[] args) {
        invoke(key, mcKey, args, null, true);
    }

    public static void invokeWrapAfterMC(String key, String mcKey, Object[] args, Object returnValue) {
        invoke(key, mcKey, args, returnValue, false);
    }

    private static void invoke(String key, String mcKey, Object[] args, Object returnValue, Boolean isBefore) {
        RuleInvokeItem item = DynamicRuleRegistry.getInstance().getRuleInvoke(key);
        try {
            List<Object> params = new ArrayList<>();
            params.add(args == null ? new Object[0] : args);

            if (item.config.needReturnValue)
                params.add(returnValue);

            if (item.config.needMethodInfo)
                params.add(item.methodInfo);

            if (item.config.needMethodCallInfo)
                params.add(item.methodInfo.get(mcKey));

            if (item.config.needPosition)
                params.add(isBefore);

            item.config.ruleMethod.invoke(item.config.ruleInstance, params.toArray());
        } catch (Exception e) {
            logger.error("Run " + item.config.position + " failed.", e);
        }
    }
}
