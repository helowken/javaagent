package agent.client;

import agent.client.command.ClientCmdRunner;
import agent.client.command.parser.impl.*;
import agent.client.command.result.handler.InfoResultHandler;
import agent.client.command.result.handler.ResetResultHandler;
import agent.client.command.result.handler.TransformResultHandler;

import java.util.Arrays;
import java.util.Collections;

import static agent.common.message.MessageType.*;

public class ClientMgr {
    private static final ClientCmdRunner cmdRunner = new ClientCmdRunner();

    static {
        cmdRunner.getCmdParseMgr()
                .reg(
                        "",
                        Collections.singletonList(
                                new ClientHelpCmdParser()
                        )
                )
                .reg(
                        "System Management:",
                        Arrays.asList(
                                new InfoCmdParser(),
                                new SearchCmdParser(),
                                new ResetCmdParser(),
                                new FlushLogCmdParser(),
                                new CommandFileCmdParser(),
                                new EchoCmdParser()
                        )
                )
                .reg(
                        "Service Management:",
                        Arrays.asList(
                                new BuiltInTransformCmdParser.CostTimeCmdParser(),
                                new BuiltInTransformCmdParser.TraceCmdParser(),
                                new JavascriptTransformCmdParser(),
                                new JavascriptConfigCmdParser(),
                                new JavascriptExecCmdParser(),
                                new StackTraceCmdParser(),
                                new SaveClassCmdParser()
                        )
                );

        InfoResultHandler infoResultHandler = new InfoResultHandler();
        cmdRunner.getExecResultMgr()
                .reg(CMD_SEARCH, infoResultHandler)
                .reg(CMD_INFO, infoResultHandler)
                .reg(CMD_TRANSFORM, new TransformResultHandler())
                .reg(CMD_RESET, new ResetResultHandler())
                .reg(CMD_SAVE_CLASS, infoResultHandler)
                .reg(CMD_JS_EXEC, infoResultHandler);
    }

    public static ClientCmdRunner getCmdRunner() {
        return cmdRunner;
    }
}
