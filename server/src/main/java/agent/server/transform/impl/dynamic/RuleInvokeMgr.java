package agent.server.transform.impl.dynamic;

import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.List;


public class RuleInvokeMgr {
    private static final Logger logger = Logger.getLogger(RuleInvokeMgr.class);

    public static void invokeBefore(String key, MethodInfo methodInfo, Object[] args) {
        invoke(key, methodInfo, args, null, null);
    }

    public static void invokeAfter(String key, MethodInfo methodInfo, Object[] args, Object returnValue) {
        invoke(key, methodInfo, args, returnValue, null);
    }

    public static void invokeWrapBefore(String key, MethodInfo methodInfo, Object[] args) {
        invoke(key, methodInfo, args, null, true);
    }

    public static void invokeWrapAfter(String key, MethodInfo methodInfo, Object[] args, Object returnValue) {
        invoke(key, methodInfo, args, returnValue, false);
    }

    private static void invoke(String key, MethodInfo methodInfo, Object[] args, Object returnValue, Boolean isBefore) {
        RuleInvokeItem item = DynamicRuleRegistry.getInstance().getRuleInvoke(key);
        try {
            List<Object> params = new ArrayList<>();
            params.add(args == null ? new Object[0] : args);

            if (item.config.needReturnValue)
                params.add(returnValue);

            if (item.config.needMethodInfo)
                params.add(methodInfo);

            if (item.config.needPosition)
                params.add(isBefore);

            item.config.ruleMethod.invoke(item.config.ruleInstance, params.toArray());
        } catch (Exception e) {
            logger.error("Run " + item.config.position + " failed.", e);
        }
    }
}
