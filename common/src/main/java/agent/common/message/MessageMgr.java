package agent.common.message;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.message.command.Command;
import agent.common.message.command.impl.*;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class MessageMgr {
    private static final Logger logger = Logger.getLogger(MessageMgr.class);
    private static final Map<Integer, Class<? extends Message>> typeToMsgClass = new HashMap<>();
    private static final LockObject typeLock = new LockObject();

    static {
        reg(MessageType.RESULT_DEFAULT, DefaultExecResult.class);
        reg(MessageType.CMD_RESET_CLASS, ResetClassCommand.class);
        reg(MessageType.CMD_TRANSFORM_BY_FILE, TransformByFileCommand.class);
        reg(MessageType.CMD_FLUSH_LOG, FlushLogCommand.class);
        reg(MessageType.CMD_ECHO, EchoCommand.class);
        reg(MessageType.CMD_TEST_CONFIG_BY_FILE, TestConfigByFileCommand.class);
        reg(MessageType.CMD_VIEW, ViewCommand.class);
        reg(MessageType.CMD_CLASSPATH, ClasspathCommand.class);
    }

    private static void reg(int type, Class<? extends Message> msgClass) {
        typeLock.sync(lock -> typeToMsgClass.put(type, msgClass));
    }

    private static Class<? extends Message> getMsgClass(int type) {
        return typeLock.syncValue(lock ->
                Optional.ofNullable(typeToMsgClass.get(type))
                        .orElseThrow(
                                () -> new RuntimeException("Unknown message type: " + type)
                        )
        );
    }

    private static synchronized Message parse(ByteBuffer bb) throws Exception {
        bb.mark();
        int type = bb.getInt();
        Message message = getMsgClass(type).newInstance();
        logger.debug("Message type: {}, version: {}", type, message.getVersion());
        bb.reset();
        message.deserialize(bb);
        return message;
    }

    public static Command parseCommand(ByteBuffer bb) throws Exception {
        return (Command) parse(bb);
    }

    public static ExecResult parseResult(ByteBuffer bb) throws Exception {
        return (ExecResult) parse(bb);
    }

}
