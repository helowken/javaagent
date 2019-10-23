package test.server.transform.config.parser;

import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.parser.RuleConfigParser;
import agent.server.transform.config.parser.RuleConfigParser.RuleConfigItem;
import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.MethodRule;
import org.junit.Test;
import test.server.AbstractServerTest;

import java.util.List;

import static agent.server.transform.config.rule.MethodRule.Position.AFTER;
import static agent.server.transform.config.rule.MethodRule.Position.BEFORE;

public class RuleTest extends AbstractServerTest {
    private static final String aContext = "/aa";
    private static final String aClassName = "aaa.A";
    private static final String aMethod = "test1";
    private static final String bContext = "/bb";
    private static final String bClassName = "bbb.A";
    private static final String bMethod = "test2";
    private static final RuleConfigParser ruleConfigParser = RuleConfigParser.getInstance();

    @Test
    public void test() {
        classFinder.setContextLoader(aContext);
        List<ModuleConfig> moduleConfigList = ruleConfigParser.parse(new RuleConfigItem(aContext, TestRule.class.getName()));
        System.out.println(moduleConfigList);
    }

    @ClassRule(aClassName)
    private static class TestRule {
        @MethodRule(method = aMethod, position = BEFORE)
        public void testBefore(Object[] args, Object rv) {
        }

        @MethodRule(method = bMethod, position = AFTER)
        public void testAfter(Object[] args, Object rv) {
        }
    }

}
