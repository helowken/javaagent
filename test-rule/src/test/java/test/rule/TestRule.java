package test.rule;

import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.ContextRule;
import agent.server.transform.config.rule.MethodRule;

import static agent.server.transform.config.rule.MethodRule.Position;

@ContextRule("/test")
@ClassRule("test.jetty.TestObject")
public class TestRule {
    @MethodRule(method = "test.*", position = Position.BEFORE)
    public void rule1(Object[] args) {
        for (int i = 0; i < args.length; ++i) {
            System.out.println("Index " + i + ": " + args[i]);
        }
    }

    @MethodRule(method = "test.*", position = Position.AFTER)
    public void rule2(Object[] args, Object returnValue) {
        System.out.println("Return value: " + returnValue);
    }
}
