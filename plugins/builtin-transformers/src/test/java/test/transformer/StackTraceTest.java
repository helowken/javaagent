package test.transformer;

import agent.builtin.tools.ResultLauncher;
import agent.client.command.parser.impl.StackTraceCmdParser;
import agent.cmdline.command.result.ExecResult;
import agent.server.command.executor.StackTraceCmdExecutor;
import org.junit.Test;
import test.server.AbstractTest;

import static org.junit.Assert.assertTrue;

public class StackTraceTest extends AbstractTest {

    @Test
    public void test2() throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    ExecResult result = new StackTraceCmdExecutor().exec(
                            new StackTraceCmdParser().parse(
                                    new String[]{
                                            "st", "-i", "7", "-c", "100",
//                                            "-ee", "test.*",
                                            "aaa", outputPath
                                    }
                            ).get(0).getCmd()
                    );
                    assertTrue(result.isSuccess());
                    Thread.sleep(2000);
                    ResultLauncher.main(
                            new String[]{"st", outputPath}
                    );
                }
        );
    }

}
