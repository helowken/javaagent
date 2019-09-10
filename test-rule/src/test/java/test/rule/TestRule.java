package test.rule;

import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.ContextRule;
import agent.server.transform.config.rule.MethodRule;
import agent.server.transform.impl.dynamic.MethodInfo;
import agent.server.transform.impl.dynamic.MethodRuleFilter;

import java.util.Collection;

import static agent.server.transform.config.rule.MethodRule.Position;

@ContextRule("/test")
@ClassRule("test.jetty.TestObject")
public class TestRule implements MethodRuleFilter {
    @MethodRule(method = "test.*", position = Position.BEFORE)
    public void rule1(Object[] args) {
        for (int i = 0; i < args.length; ++i) {
            System.out.println("Index " + i + ": " + args[i]);
        }
    }

    @MethodRule(method = "test.*", position = Position.AFTER, filter = "test.rule.TestRule")
    public void rule2(Object[] args, Object returnValue) {
        System.out.println("Return value: " + returnValue);
    }

    @Override
    public boolean accept(MethodInfo methodInfo) {
        return true;
    }

    @Override
    public boolean stepInto(MethodInfo methodInfo) {
        return true;
    }

}
