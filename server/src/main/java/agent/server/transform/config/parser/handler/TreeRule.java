package agent.server.transform.config.parser.handler;

import agent.server.transform.impl.dynamic.MethodRuleFilter;
import agent.server.transform.impl.dynamic.rule.TraverseRule;

public interface TreeRule extends MethodRuleFilter {
    String getContext();

    String getTargetClass();

    String getTargetMethod();

    int getMaxLevel();

    TraverseRule getTraverseRule();
}
