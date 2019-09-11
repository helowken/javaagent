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
import agent.server.transform.impl.dynamic.MethodInfo;
import agent.server.transform.impl.dynamic.MethodRuleFilter;
import agent.server.transform.impl.dynamic.rule.TreeTimeMeasureRule;
import agent.server.transform.impl.utils.AgentClassPool;
import org.junit.BeforeClass;
import org.junit.Test;
import test.AbstractTest;
import test.rule.TestRuleTest;
import test.utils.TestClassFinder;
import test.utils.TestClassLoader;
import test.utils.TestInstrumentation;
import test.utils.TestMap;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static agent.server.transform.config.rule.MethodRule.Position.*;

public class TestConfigRuleTest extends AbstractTest {
    private static final String context = "/test";
    private static final String bIntfClassName = "test.flow.TestConfigRuleTest$BIntf";
    private static final String baseAClassName = "test.flow.TestConfigRuleTest$BaseA";
    private static final String abstractBClassName = "test.flow.TestConfigRuleTest$AbstractB";
    private static final String b1ClassName = "test.flow.TestConfigRuleTest$B1";
    private static final String b2ClassName = "test.flow.TestConfigRuleTest$B2";
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

    @Test
    public void test111() throws Exception {
        ReflectionUtils.invokeMethod(BIntf.class, "task1", new Class[0], method -> {
            System.out.println(Modifier.isAbstract(method.getModifiers()));
            return null;
        });
        ReflectionUtils.invokeMethod(B1.class, "task1", new Class[0], method -> {
            System.out.println(method.getDeclaringClass());
            return null;
        });
        ReflectionUtils.invokeMethod(B2.class, "task1", new Class[0], method -> {
            System.out.println(method.getDeclaringClass());
            return null;
        });
        AgentClassPool pool = AgentClassPool.getInstance();
        System.out.println(pool.get(b1ClassName).subclassOf(pool.get(bIntfClassName)));
        System.out.println(pool.get(abstractBClassName).subclassOf(pool.get(bIntfClassName)));
        System.out.println(pool.get(b1ClassName).subclassOf(pool.get(abstractBClassName)));

        System.out.println(pool.get(b1ClassName).subtypeOf(pool.get(bIntfClassName)));
        System.out.println(pool.get(abstractBClassName).subtypeOf(pool.get(bIntfClassName)));
        System.out.println(pool.get(b1ClassName).subtypeOf(pool.get(abstractBClassName)));
    }

    @Test
    public void testTimeRule() throws Exception {
        // import class B1, B2
        new B1();
        new B2();
        new TestMap();

        Command cmd = new ByRuleCommand.TransformByRuleCommand(context, TestTimeRule.class.getName());
        ExecResult result = new TransformCmdExecutor().exec(cmd);
        CommandResultHandlerMgr.handleResult(cmd, result);

        classloader.defineClass(baseAClassName, instrumentation.getBytes(baseAClassName));
        classloader.defineClass(bIntfClassName, AgentClassPool.getInstance().get(bIntfClassName).toBytecode());
        classloader.defineClass(abstractBClassName, instrumentation.getBytes(abstractBClassName));
        classloader.defineClass(b1ClassName, instrumentation.getBytes(b1ClassName));
        classloader.defineClass(b2ClassName, instrumentation.getBytes(b2ClassName));
        Class<?> aClass = classloader.defineClass(aClassName, instrumentation.getBytes(aClassName));
        Object a = ReflectionUtils.newInstance(aClass);
        ReflectionUtils.invoke("runTasks", a);
    }

    @ContextRule(context)
    @ClassRule(aClassName)
    public static class TestRule {
        @MethodRule(method = "test.*", position = BEFORE)
        public void methodBefore(Object[] args) {
            System.out.println("Args: " + Arrays.toString(args));
        }

        @MethodRule(method = "test.*", position = AFTER)
        public void methodAfter(Object[] args, Object returnValue) {
            System.out.println("Return value: " + returnValue);
        }

        @MethodRule(method = "run", position = WRAP_MC)
        public void methodThrough(Object[] args, Object returnValue, MethodInfo mcInfo, boolean isBefore) {
            System.out.println("-------------------------------------");
            System.out.print(mcInfo + ", args: " + Arrays.toString(args));
            if (!isBefore)
                System.out.print(", return value: " + returnValue);
            System.out.println();
        }
    }

    @ContextRule(context)
    @ClassRule(aClassName)
    public static class TestTimeRule extends TreeTimeMeasureRule implements MethodRuleFilter {
        @MethodRule(method = "runTasks", position = WRAP)
        public void wrap(Object[] args, Object returnValue, MethodInfo methodInfo, boolean isBefore) {
            if (isBefore)
                super.methodStart(args, methodInfo);
            else
                super.methodEnd(returnValue);
        }

        @MethodRule(method = "runTasks", position = WRAP_MC, maxLevel = 20, filter = "test.flow.TestConfigRuleTest$TestTimeRule")
        public void wrapMC(Object[] args, Object returnValue, MethodInfo methodInfo, boolean isBefore) {
            if (isBefore)
                super.methodCallStart(args, methodInfo);
            else
                super.methodCallEnd(returnValue);
        }

        @Override
        public boolean accept(MethodInfo methodInfo) {
//            return !ReflectionUtils.isJavaNativePackage(methodInfo.className);
//            return methodInfo.methodName.contains("task3111");
//            return methodInfo.className.equals(aClassName);
            return methodInfo.className.startsWith("test.flow.");
        }

        @Override
        public boolean stepInto(MethodInfo methodInfo) {
//            return methodInfo.className.equals(aClassName);
            return methodInfo.className.startsWith("test.flow.");
        }

        @Override
        public Collection<String> getImplClasses(MethodInfo methodInfo, Collection<String> loadedSubClassNames) {
            return loadedSubClassNames.stream()
                    .filter(className -> className.startsWith("test.flow."))
                    .collect(Collectors.toList());
        }
    }

    static abstract class BaseA {
        private BIntf b1 = new B1();
        private BIntf b2 = new B2();

        void task4() throws Exception {
            System.out.println("------ task4-------");
            Thread.sleep(40);
        }

        BIntf getB(int v) {
            return v == 0 ? b1 : b2;
        }
    }

    static class A extends BaseA {
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

        public void runTasks() throws Exception {
            getB(0).task1();
            task2();
            task3();
            getB(1).task1();
        }


        private void task2() throws Exception {
            Thread.sleep(20);
            task21();
        }

        private void task21() throws Exception {
            Thread.sleep(20);
            task211();
        }

        private void task211() throws Exception {
            Thread.sleep(20);
            task4();
        }

        private void task3() throws Exception {
            Thread.sleep(30);
            task31();
        }

        private void task31() throws Exception {
            Thread.sleep(30);
            task311();
        }

        private void task311() throws Exception {
            Thread.sleep(30);
            task3111();
        }

        private void task3111() throws Exception {
            Thread.sleep(30);
            task4();
        }

    }

    interface BIntf {
        void task1() throws Exception;
    }

    private static abstract class AbstractB implements BIntf {
        private Map<String, Object> map = new HashMap<>();

        public void task1() throws Exception {
            map.put("aaa", 111);
            if (map.containsKey("bbb"))
                System.out.println(111);
            Map map = new TestMap();
            map.put("111", "333");
            Thread.sleep(30);
            doTask();
        }

        abstract void doTask() throws Exception;
    }

    private static class B1 extends AbstractB {
        void doTask() throws Exception {
            Thread.sleep(10);
            task11();
        }

        private void task11() throws Exception {
            Thread.sleep(10);
        }
    }

    private static class B2 extends AbstractB {
        void doTask() throws Exception {
            Thread.sleep(20);
            task21();
        }

        private void task21() throws Exception {
            Thread.sleep(20);
        }
    }
}
