package agent.client.command.result.handler;

import agent.base.utils.LockObject;
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
        regResultHandlerClass(CMD_TEST_CONFIG, new TestConfigResultHandler());
        regResultHandlerClass(CMD_VIEW, new ViewResultHandler());
        regResultHandlerClass(CMD_TRANSFORM_CLASS, new TransformClassResultHandler());
        regResultHandlerClass(CMD_RESET_CLASS, new ResetClassResultHandler());
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
