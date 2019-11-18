package test.server;

import agent.base.plugin.PluginFactory;
import agent.base.utils.SystemConfig;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.App;
import agent.server.transform.TransformMgr;
import org.junit.After;
import org.junit.BeforeClass;
import test.server.utils.TestClassFinder;
import test.server.utils.TestClassLoader;
import test.server.utils.TestInstrumentation;

import java.util.Properties;

public abstract class AbstractServerTest {
    protected static final String defaultContext = "DEFAULT_CONTEXT";
    protected static final TestClassFinder classFinder = new TestClassFinder();
    protected static final TestInstrumentation instrumentation = new TestInstrumentation();
    private static boolean inited = false;

    @BeforeClass
    public static void beforeClass() {
        init();
    }

    @After
    public void afterClass() {
        classFinder.reset();
    }

    private static synchronized void init() {
        if (!inited) {
            App.instance = new Object();
            SystemConfig.load(new Properties());
            PluginFactory.setMock(ClassFinder.class, classFinder);
            TransformMgr.getInstance().init(instrumentation);
            inited = true;
        }
    }
}
