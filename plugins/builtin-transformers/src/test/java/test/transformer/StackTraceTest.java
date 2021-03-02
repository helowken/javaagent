package test.transformer;

import agent.builtin.tools.result.StackTraceResultHandler;
import agent.builtin.tools.result.parse.StackTraceResultParamParser;
import agent.builtin.tools.result.parse.StackTraceResultParams;
import agent.client.ClientMgr;
import agent.cmdline.command.result.ExecResult;
import agent.server.command.executor.ServerCmdExecMgr;
import org.junit.Test;
import test.server.AbstractTest;

import static org.junit.Assert.assertTrue;

public class StackTraceTest extends AbstractTest {

    @Test
    public void test2() throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    ExecResult result = ServerCmdExecMgr.exec(
                            ClientMgr.getCmdRunner().getCmdParseMgr().parse(
                                    "st",
                                    new String[]{
                                            "-i", "7",
                                            "-c", "100",
//                                            "-ee", "test.*",
                                            "keyAAA",
                                            outputPath
                                    }
                            ).get(0).getCmd()
                    );
                    assertTrue(result.isSuccess());
                    Thread.sleep(2000);

                    StackTraceResultParams params = new StackTraceResultParamParser().parse(
                            new String[]{outputPath}
                    );
                    new StackTraceResultHandler().exec(params);
                }
        );
    }

    @Test
    public void test3() throws Exception {
        StackTraceResultParams params = new StackTraceResultParamParser().parse(
                new String[]{"/home/helowken/cost-time/st"}
        );
        new StackTraceResultHandler().exec(params);
    }
}
