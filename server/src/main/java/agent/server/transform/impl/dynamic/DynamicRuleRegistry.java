package agent.server.transform.impl.dynamic;

import agent.common.utils.Registry;

import java.util.function.Function;

public class DynamicRuleRegistry {
    private static final DynamicRuleRegistry instance = new DynamicRuleRegistry();
    private final Registry<String, RuleInvokeItem> ruleInvokeRegistry = new Registry<>();
    private final Registry<String, MethodInfo> methodInfoRegistry = new Registry<>();

    public static DynamicRuleRegistry getInstance() {
        return instance;
    }

    private DynamicRuleRegistry() {
    }

    void regRuleInvokeIfAbsent(String key, Function<String, RuleInvokeItem> supplier) {
        ruleInvokeRegistry.regIfAbsent(key, supplier);
    }

    public RuleInvokeItem getRuleInvoke(String key) {
        return ruleInvokeRegistry.get(key);
    }

    MethodInfo regMethodInfoIfAbsent(String key, Function<String, MethodInfo> supplier) {
        return methodInfoRegistry.regIfAbsent(key, supplier);
    }
}
