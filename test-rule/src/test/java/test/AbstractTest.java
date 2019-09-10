package test;

import agent.base.plugin.PluginFactory;
import agent.jvmti.JvmtiUtils;
import org.junit.After;
import org.junit.BeforeClass;

import static test.utils.ServerTestUtils.initSystemConfig;
import static test.utils.ServerTestUtils.mockClassFinder;

public class AbstractTest {
    private static final JvmtiUtils jvmtiUtils = JvmtiUtils.getInstance();

    static {
        jvmtiUtils.load(System.getProperty("user.dir") + "/../packaging/resources/server/native/libagent_jvmti_JvmtiUtils.so");
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
