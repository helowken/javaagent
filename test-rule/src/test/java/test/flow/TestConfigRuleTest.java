package test.flow;

import agent.base.plugin.PluginFactory;
import agent.client.command.result.handler.TestConfigResultHandler;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ByRuleCommand;
import agent.common.message.result.ExecResult;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.App;
import agent.server.command.executor.TestConfigCmdExecutor;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.ContextRule;
import agent.server.transform.config.rule.MethodRule;
import org.junit.Test;
import test.AbstractTest;
import test.utils.TestClassFinder;
import test.utils.TestClassLoader;

import java.net.URL;

public class TestConfigRuleTest extends AbstractTest {
    private static final String context = "/test";

    @Test
    public void test2() {
        App.instance = getClass().getName();
        TestClassFinder classFinder = new TestClassFinder();
        classFinder.set(context, new TestClassLoader());
        PluginFactory.setMock(ClassFinder.class, classFinder);

        URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
        TransformMgr.getInstance().addClasspath(
                context,
                url
        );

        Command cmd = new ByRuleCommand.TestConfigByRuleCommand(context, TestRule.class.getName());
        ExecResult result = new TestConfigCmdExecutor().exec(cmd);
        new TestConfigResultHandler().handle(cmd, result);
    }

    @ContextRule(context)
    @ClassRule("test.flow.TestConfigRuleTest$A")
    static class TestRule {
        @MethodRule(method = "test.*", position = MethodRule.Position.BEFORE)
        public void callRule() {
        }
    }

    static class A {
        public void test() {
        }

        public void test2(String s) {
        }

        public void test3(int a, float b, String c) {
        }
    }
}
