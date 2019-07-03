package test.server;

import org.junit.Test;
import agent.server.AgentServerMgr;
import agent.server.transform.TransformMgr;

public class TestAgentServer {
//    @Test
    public void test() throws Exception {
        if (!AgentServerMgr.startup(10086)) {
            return;
        }
        TransformMgr.getInstance().init(new TestInstrumentation());
        Thread.sleep(1000000);
    }
}
