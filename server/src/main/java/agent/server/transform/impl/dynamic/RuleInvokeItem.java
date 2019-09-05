package agent.server.transform.impl.dynamic;

public class RuleInvokeItem {
    final DynamicConfigItem config;
    final MethodInfo methodInfo;

    RuleInvokeItem(DynamicConfigItem config, MethodInfo methodInfo) {
        this.config = config;
        this.methodInfo = methodInfo;
    }
}
