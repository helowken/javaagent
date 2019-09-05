package test.flow;

import agent.base.plugin.PluginFactory;
import agent.base.utils.ReflectionUtils;
import agent.client.command.result.handler.CommandResultHandlerMgr;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ByRuleCommand;
import agent.common.message.result.ExecResult;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.App;
import agent.server.command.executor.TestConfigCmdExecutor;
import agent.server.command.executor.TransformCmdExecutor;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.ContextRule;
import agent.server.transform.config.rule.MethodRule;
import agent.server.transform.impl.dynamic.MethodCallFilter;
import agent.server.transform.impl.dynamic.MethodCallInfo;
import org.junit.BeforeClass;
import org.junit.Test;
import test.AbstractTest;
import test.utils.TestClassFinder;
import test.utils.TestClassLoader;
import test.utils.TestInstrumentation;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;

public class TestConfigRuleTest extends AbstractTest {
    private static final String context = "/test";
    private static final String aClassName = "test.flow.TestConfigRuleTest$A";
    private static final TestClassLoader classloader = new TestClassLoader();
    private static final TestInstrumentation instrumentation = new TestInstrumentation();

    @BeforeClass
    public static void testConfigRuleTestBeforeClass() {
        Class<?> clazz = TestConfigRuleTest.class;
        App.instance = clazz.getName();
        TestClassFinder classFinder = new TestClassFinder();
        classFinder.set(context, classloader);
        PluginFactory.setMock(ClassFinder.class, classFinder);

        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        TransformMgr.getInstance().addClasspath(
                context,
                url
        );

        TransformMgr.getInstance().init(instrumentation);
    }

    @Test
    public void testTestConfigRule() {
        Command cmd = new ByRuleCommand.TestConfigByRuleCommand(context, TestRule.class.getName());
        ExecResult result = new TestConfigCmdExecutor().exec(cmd);
        CommandResultHandlerMgr.handleResult(cmd, result);
    }

    @Test
    public void testTransformRule() throws Exception {
        Command cmd = new ByRuleCommand.TransformByRuleCommand(context, TestRule.class.getName());
        ExecResult result = new TransformCmdExecutor().exec(cmd);
        CommandResultHandlerMgr.handleResult(cmd, result);

        Class<?> aClass = classloader.defineClass(aClassName, instrumentation.getBytes(aClassName));
        Object a = ReflectionUtils.newInstance(aClass);
        ReflectionUtils.invoke("test", a);
        ReflectionUtils.invoke("test2", a, "aaa");
//        ReflectionUtils.invoke("test3", a, 1, 3.5f, "bbb");
        ReflectionUtils.invoke("run", a);
    }

    @ContextRule(context)
    @ClassRule(aClassName)
    public static class TestRule {
        @MethodRule(method = "test.*", position = MethodRule.Position.BEFORE)
        public void methodBefore(Object[] args) {
            System.out.println("Args: " + Arrays.toString(args));
        }

        @MethodRule(method = "test.*", position = MethodRule.Position.AFTER)
        public void methodAfter(Object[] args, Object returnValue) {
            System.out.println("Return value: " + returnValue);
        }

        @MethodRule(method = "run", position = MethodRule.Position.WRAP_MC)
        public void methodThrough(Object[] args, Object returnValue, MethodCallInfo mcInfo, boolean isBefore) {
            System.out.println("-------------------------------------");
            System.out.print(mcInfo + ", args: " + Arrays.toString(args));
            if (!isBefore)
                System.out.print(", return value: " + returnValue);
            System.out.println();
        }
    }

    public static class TestRuleMcFilter implements MethodCallFilter {
        @Override
        public boolean accept(MethodCallInfo methodCallInfo) {
            return false;
        }
    }

    static class A {
        public void test() {
        }

        public void test2(String s) {
        }

        public void test3(int a, float b, String c) {
        }

        public Date run() {
            System.out.println("11111");
            String b = "a".trim();
            b += 222;
            Date d = new Date();
            long c = d.getTime();
            System.out.println("b: " + b + ", c: " + c);
            return new Date(c);
        }
    }
}
