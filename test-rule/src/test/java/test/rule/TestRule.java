package test.rule;

import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.ContextRule;
import agent.server.transform.config.rule.MethodRule;

import java.util.List;

import static agent.server.transform.config.rule.MethodRule.Position;

@ContextRule("/test")
@ClassRule("test.jetty.TestObject")
public class TestRule {
    @MethodRule(method = "test", position = Position.BEFORE)
    public void rule1(List<Object> args, Object rv) {

    }
}
