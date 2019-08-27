package test.server;

import agent.base.plugin.PluginFactory;
import org.junit.After;
import org.junit.BeforeClass;

import static test.server.utils.ServerTestUtils.initSystemConfig;
import static test.server.utils.ServerTestUtils.mockClassFinder;

public abstract class AbstractServerTest {
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
