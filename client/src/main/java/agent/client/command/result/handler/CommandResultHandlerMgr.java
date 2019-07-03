package agent.client.command.result.handler;

import agent.base.utils.LockObject;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.message.result.handler.ExecResultHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandResultHandlerMgr {
    private static final Map<Integer, ExecResultHandler> typeToResultHandler = new HashMap<>();
    private static final LockObject resultHandlerLock = new LockObject();

    public static void regResultHandlerClass(int type, ExecResultHandler rsHandler) {
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
