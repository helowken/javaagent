package test.server.transform.config.rule;

import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.ContextRule;
import agent.server.transform.config.rule.MethodRule;
import org.junit.Test;

import static agent.server.transform.config.rule.MethodRule.Position.AFTER;
import static agent.server.transform.config.rule.MethodRule.Position.BEFORE;

public class RuleTest {
    private static final String aContext = "/aa";
    private static final String aClassName = "aaa.A";
    private static final String bContext = "/bb";
    private static final String bClassName = "bbb.A";

    @Test
    public void test() {

    }

    @ContextRule(aContext)
    @ClassRule(aClassName)
    private static class TestRule {
        @MethodRule(method = "", position = BEFORE)
        public void testBefore(Object[] args, Object rv) {
        }

        @ContextRule(bContext)
        @ClassRule(bClassName)
        @MethodRule(method = "call", position = AFTER)
        public void testAfter(Object[] args, Object rv) {
        }
    }
}
