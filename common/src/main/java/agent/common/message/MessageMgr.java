package agent.common.message;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.message.command.Command;
import agent.common.message.command.CommandType;
import agent.common.message.command.impl.*;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.message.result.ResultType;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class MessageMgr {
    private static final Logger logger = Logger.getLogger(MessageMgr.class);
    private static final Map<Integer, Class<? extends Message>> typeToCmdClass = new HashMap<>();
    private static final LockObject cmdLock = new LockObject();
    private static final Map<Integer, Class<? extends Message>> typeToResultClass = new HashMap<>();
    private static final LockObject resultLock = new LockObject();

    static {
        regCmdClass(CommandType.CMD_TYPE_RESET_CLASS, ResetClassCommand.class);
        regCmdClass(CommandType.CMD_TYPE_TRANSFORM_CLASS, TransformClassCommand.class);
        regCmdClass(CommandType.CMD_TYPE_FLUSH_LOG, FlushLogCommand.class);
        regCmdClass(CommandType.CMD_TYPE_ECHO, EchoCommand.class);
        regCmdClass(CommandType.CMD_TYPE_TEST_CONFIG, TestConfigCommand.class);
        regCmdClass(CommandType.CMD_TYPE_VIEW, ViewCommand.class);

        regResultClass(ResultType.RS_TYPE_DEFAULT, DefaultExecResult.class);
    }

    private static void regCmdClass(int type, Class<? extends Command> cmdClass) {
        cmdLock.sync(lock -> typeToCmdClass.put(type, cmdClass));
    }

    private static void regResultClass(int type, Class<? extends ExecResult> rsClass) {
        resultLock.sync(lock -> typeToResultClass.put(type, rsClass));
    }


    private static synchronized Message parse(Map<Integer, Class<? extends Message>> map, ByteBuffer bb, String msg) throws Exception {
        bb.mark();
        int type = bb.getInt();
        logger.debug("{} type: {}", msg, type);
        Message message = Optional.ofNullable(map.get(type))
                .orElseThrow(() -> new RuntimeException("Unknown " + msg + " type: " + type))
                .newInstance();
        logger.debug("{} version: {}", msg, message.getVersion());
        bb.reset();
        message.deserialize(bb);
        return message;
    }

    public static Command parseCommand(ByteBuffer bb) throws Exception {
        return (Command) parse(typeToCmdClass, bb, "Command");
    }

    public static ExecResult parseResult(ByteBuffer bb) throws Exception {
        return (ExecResult) parse(typeToResultClass, bb, "Result");
    }

}
