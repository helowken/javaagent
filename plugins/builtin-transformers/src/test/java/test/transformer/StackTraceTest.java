package test.transformer;

import agent.builtin.tools.result.StackTraceResultHandler;
import agent.builtin.tools.result.parse.StackTraceResultParamParser;
import agent.builtin.tools.result.parse.StackTraceResultParams;
import agent.client.command.parser.CommandParserMgr;
import agent.common.message.result.ExecResult;
import agent.server.command.executor.CmdExecutorMgr;
import org.junit.Test;
import test.server.AbstractTest;

import static org.junit.Assert.assertTrue;

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
                                            "-rs",
//                                            "-ee", "test.*",
                                            "keyAAA",
                                            outputPath
                                    }
                            ).get(0).getCmd()
                    );
                    assertTrue(result.isSuccess());
                    Thread.sleep(2000);

                    StackTraceResultParams params = new StackTraceResultParamParser().parse(
                            new String[]{"-rs", outputPath}
                    );
                    new StackTraceResultHandler().exec(params);
                }
        );
    }

    @Test
    public void test3() throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    ExecResult result = CmdExecutorMgr.exec(
                            CommandParserMgr.parse(
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
}
