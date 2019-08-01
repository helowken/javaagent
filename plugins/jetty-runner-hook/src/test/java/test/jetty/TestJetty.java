package test.jetty;

import agent.base.utils.IndentUtils;
import agent.base.utils.ReflectionUtils;
import agent.jvmti.JvmtiUtils;
import org.eclipse.jetty.runner.Runner;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestJetty {

    @Test
    public void test1() throws Exception {
        doTest(runner -> {
            Object server = ReflectionUtils.getFieldValue("_server", runner);
            assertNotNull(server);

            List objects = ReflectionUtils.getFieldValue("_beans", server);
            assertNotNull(objects);
            Object handlerCollection = findBean(objects, HandlerCollection.class);

            Object[] objArray = ReflectionUtils.getFieldValue("_handlers", handlerCollection);
            assertNotNull(objArray);
            Object contextHandlerCollection = find(Arrays.asList(objArray), ContextHandlerCollection.class);

            objects = ReflectionUtils.getFieldValue("_beans", contextHandlerCollection);
            assertNotNull(objects);
            for (Object object : objects) {
                Object context = ReflectionUtils.getFieldValue("_bean", object);
                assertEquals(context.getClass(), WebAppContext.class);

                Object classLoader = ReflectionUtils.getFieldValue("_classLoader", context);
                assertNotNull(classLoader);

                String contextPath = ReflectionUtils.invoke("getContextPath", context);
                System.out.println(contextPath + ": " + classLoader);
            }
        });
    }

    @Test
    public void test2() throws Exception {
        doTest(runner -> {
            Object contexts = ReflectionUtils.getFieldValue("_contexts", runner);
            Object[] handlers = ReflectionUtils.invoke("getHandlers", contexts);
            ClassLoader tmpLoader = null;
            for (Object handler : handlers) {
                String contextPath = ReflectionUtils.invoke("getContextPath", handler);
                ClassLoader loader = ReflectionUtils.invoke("getClassLoader", handler);
                System.out.println(contextPath + ": " + loader);
                tmpLoader = loader;
            }

            int level = 0;
            while (tmpLoader != null) {
                System.out.println(IndentUtils.getIndent(level++) + tmpLoader.getClass().getName());
                tmpLoader = tmpLoader.getParent();
            }
        });
    }

    @Test
    public void test3() throws Exception {
        doTest(runner -> {
            System.load("/home/helowken/test_jni/jni_jvmti/libagent_jvmti_JvmtiUtils.so");
            List loaders = JvmtiUtils.getInstance().findObjectsByClass(WebAppClassLoader.class, Integer.MAX_VALUE);
            for (Object loader : loaders) {
                System.out.println(loader);
            }
        });
    }

    private void doTest(RunFunc func) throws Exception {
        Runner runner = new Runner();
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                func.run(runner);
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        String dir = "/home/helowken/test_jetty";
        String[] args = new String[]{
                "--lib", "/home/helowken/test_agent/common-lib",
                "--stop-port", "9100",
                "--stop-key", "stop_test_jetty",
                "--config", dir + "/config/jetty-ssl.xml",
                "--config", dir + "/config/jetty.xml",
                dir + "/config/test.war.xml",
                dir + "/config/test2.war.xml"
        };
        runner.configure(args);
        runner.run();
    }

    private Object findBean(List objects, Class<?> clazz) throws Exception {
        List beans = new ArrayList();
        for (Object obj : objects) {
            beans.add(ReflectionUtils.getFieldValue("_bean", obj));
        }
        return find(beans, clazz);
    }

    private Object find(List objects, Class<?> clazz) {
        for (Object obj : objects) {
            if (obj.getClass().equals(clazz))
                return obj;
        }
        fail();
        return null;
    }

    interface RunFunc {
        void run(Runner runner) throws Exception;
    }
}
