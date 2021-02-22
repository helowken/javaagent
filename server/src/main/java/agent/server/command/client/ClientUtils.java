package agent.server.command.client;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.utils.DependentClassItem;

import java.util.List;

public class ClientUtils {
    private static final String COMMAND_PARSER_DELEGATE_CLASS = "agent.client.command.parser.CommandParserMgr";
    private static final String RESULT_HANDLER_DELEGATE_CLASS = "agent.client.command.result.CommandResultHandlerMgr";
    private static final DependentClassItem item = DependentClassItem.getInstance();

    public static List<CmdItem> parseCommand(String cmd, String[] args) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        item.getDelegateClass(COMMAND_PARSER_DELEGATE_CLASS),
                        "parse",
                        new Class[]{
                                String.class,
                                String[].class
                        },
                        cmd,
                        args
                )
        );
    }

    public static void handleResult(Command cmd, ExecResult result) {
        Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        item.getDelegateClass(RESULT_HANDLER_DELEGATE_CLASS),
                        "handleResult",
                        new Class[]{
                                Command.class,
                                ExecResult.class
                        },
                        cmd,
                        result
                )
        );
    }

}
