package agent.client;

import agent.base.runner.Runner;
import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.client.command.RemoteCommandExecutor;
import agent.client.command.parser.impl.*;
import agent.client.command.result.handler.InfoResultHandler;
import agent.client.command.result.handler.TransformResultHandler;
import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.command.runner.CommandRunner;

import java.util.List;

import static agent.common.message.MessageType.*;

public class AgentClientRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AgentClientRunner.class);
    private RemoteCommandExecutor cmdExecutor;

    static {
        Logger.setAsync(false);
    }

    @Override
    public void startup(Object... args) {
        cmdExecutor = new RemoteCommandExecutor(
                Utils.getArgValue(args, 0)
        );
        List<OptConfig> globalOptConfigs = Utils.getArgValue(args, 1);
        List<String> cmdArgs = Utils.getArgValue(args, 2);
        logger.debug("Cmd args: {}", cmdArgs);

        InfoResultHandler infoResultHandler = new InfoResultHandler();
        CommandRunner.getInstance()
                .regParse(
                        new ClientHelpCmdParser(globalOptConfigs)
                )
                .regParse(
                        "System Management:",
                        new InfoCmdParser(),
                        new SearchCmdParser(),
                        new ResetCmdParser(),
                        new FlushLogCmdParser(),
                        new CommandFileCmdParser(),
                        new EchoCmdParser()
                )
                .regParse(
                        "Service Management:",
                        new BuiltInTransformCmdParser.CostTimeCmdParser(),
                        new BuiltInTransformCmdParser.TraceCmdParser(),
                        new JavascriptTransformCmdParser(),
                        new JavascriptConfigCmdParser(),
                        new JavascriptExecCmdParser(),
                        new StackTraceCmdParser(),
                        new SaveClassCmdParser()
                )
                .setDefaultExecutor(cmdExecutor)
                .regResult(
                        infoResultHandler,
                        CMD_SEARCH,
                        CMD_INFO,
                        CMD_RESET,
                        CMD_SAVE_CLASS,
                        CMD_JS_EXEC
                )
                .regResult(
                        new TransformResultHandler(),
                        CMD_TRANSFORM
                )
                .setCmdNotFoundHandler(
                        e -> ConsoleLogger.getInstance().error(
                                "{}",
                                "Type 'ja help' to get a list of global options and commands."
                        )
                )
                .run(cmdArgs);
    }

    @Override
    public void shutdown() {
        cmdExecutor.shutdown();
    }

}
