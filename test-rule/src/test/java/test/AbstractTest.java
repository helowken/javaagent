package test;

import agent.base.plugin.PluginFactory;
import agent.base.utils.Logger;
import org.junit.After;
import org.junit.BeforeClass;

import static test.utils.ServerTestUtils.initSystemConfig;
import static test.utils.ServerTestUtils.mockClassFinder;

public class AbstractTest {

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
