package test;

import agent.base.plugin.PluginFactory;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.App;
import agent.server.transform.ContextClassLoaderMgr;
import agent.server.transform.TransformMgr;
import org.junit.After;
import org.junit.BeforeClass;
import test.utils.TestClassFinder;
import test.utils.TestClassLoader;
import test.utils.TestInstrumentation;

import java.net.URL;

import static test.utils.ServerTestUtils.initSystemConfig;
import static test.utils.ServerTestUtils.mockClassFinder;

public class AbstractTest {
    public static final String context = "/test";
    protected static final TestClassLoader classloader = new TestClassLoader();
    protected static final TestInstrumentation instrumentation = new TestInstrumentation();

    protected static void init(Class<?> clazz) {
        App.instance = clazz.getName();
        TestClassFinder classFinder = new TestClassFinder();
        classFinder.set(context, classloader);
        PluginFactory.setMock(ClassFinder.class, classFinder);

        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        ContextClassLoaderMgr.getInstance().addClasspath(
                context,
                url
        );

        TransformMgr.getInstance().onStartup(new Object[]{instrumentation});
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        initSystemConfig();
        mockClassFinder();
    }

    @After
    public void after() {
        PluginFactory.clearMocks();
    }
}
