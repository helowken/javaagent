package agent.server.transform.config.parser.handler;

import agent.server.transform.impl.dynamic.MethodRuleFilter;

public interface TreeTimeRuleConfig extends MethodRuleFilter {
    String getContext();

    String getClassName();

    String getMethod();

    int getMaxLevel();
}
