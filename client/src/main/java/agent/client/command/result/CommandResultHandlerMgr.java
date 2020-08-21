package agent.client.command.result;

import agent.base.utils.LockObject;
import agent.client.command.result.handler.DefaultExecResultHandler;
import agent.client.command.result.handler.ResetResultHandler;
import agent.client.command.result.handler.TransformResultHandler;
import agent.client.command.result.handler.InfoResultHandler;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.message.result.handler.ExecResultHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static agent.common.message.MessageType.*;

public class CommandResultHandlerMgr {
    private static final Map<Integer, ExecResultHandler> typeToResultHandler = new HashMap<>();
    private static final LockObject resultHandlerLock = new LockObject();

    static {
        InfoResultHandler viewResultHandler = new InfoResultHandler();

        regResultHandlerClass(CMD_SEARCH, viewResultHandler);
        regResultHandlerClass(CMD_VIEW, viewResultHandler);
        regResultHandlerClass(CMD_TRANSFORM, new TransformResultHandler());
        regResultHandlerClass(CMD_RESET, new ResetResultHandler());
    }

    private static void regResultHandlerClass(int type, ExecResultHandler rsHandler) {
        resultHandlerLock.sync(lock -> typeToResultHandler.put(type, rsHandler));
    }

    public static void handleResult(Command cmd, ExecResult result) {
        resultHandlerLock.sync(lock ->
                Optional.ofNullable(typeToResultHandler.get(cmd.getType()))
                        .orElse(DefaultExecResultHandler.getInstance())
                        .handle(cmd, result)
        );
    }
}
