package test.transformer;

import agent.builtin.tools.result.StackTraceResultHandler;
import agent.builtin.tools.result.parse.StackTraceResultParamParser;
import agent.builtin.tools.result.parse.StackTraceResultParams;
import agent.client.command.parser.CommandParserMgr;
import agent.common.message.result.ExecResult;
import agent.common.message.result.ResultStatus;
import agent.server.command.executor.CmdExecutorMgr;
import org.junit.Test;
import test.server.AbstractTest;

import static org.junit.Assert.assertEquals;

public class StackTraceTest extends AbstractTest {
    @Test
    public void test() throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    ExecResult result = CmdExecutorMgr.exec(
                            CommandParserMgr.parse(
                                    "st",
                                    new String[]{
                                            "-i", "7",
                                            "-c", "100",
                                            outputPath
                                    }
                            ).getCmd()
                    );
                    assertEquals(ResultStatus.SUCCESS, result.getStatus());
                    Thread.sleep(5000);

                    StackTraceResultParams params = new StackTraceResultParamParser().parse(
                            new String[]{outputPath}
                    );
                    new StackTraceResultHandler().exec(params);
                }
        );
    }
}
