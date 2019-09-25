package agent.server.transform.impl.dynamic.rule;

import agent.server.transform.config.rule.MethodRule;
import agent.server.transform.config.rule.MethodRule.Position;
import agent.server.transform.impl.dynamic.MethodInfo;

public interface TraverseRule {
    @MethodRule(method = "", position = Position.WRAP)
    void methodWrap(Object[] args, Object returnValue, MethodInfo methodInfo, boolean before);

    @MethodRule(method = "", position = Position.WRAP_MC)
    void methodCallWrap(Object[] args, Object returnValue, MethodInfo methodInfo, boolean before);
}
