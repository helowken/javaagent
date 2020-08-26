package agent.common.message;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.message.command.Command;
import agent.common.message.command.impl.EchoCommand;
import agent.common.message.command.impl.FlushLogCommand;
import agent.common.message.command.impl.MapCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class MessageMgr {
    private static final Logger logger = Logger.getLogger(MessageMgr.class);
    private static final Map<Integer, Function<Integer, Message>> typeToCreator = new HashMap<>();
    private static final LockObject typeLock = new LockObject();

    static {
        reg(MessageType.RESULT_DEFAULT, type -> new DefaultExecResult());
        reg(MessageType.CMD_RESET, MapCommand::new);
        reg(MessageType.CMD_TRANSFORM, MapCommand::new);
        reg(MessageType.CMD_FLUSH_LOG, type -> new FlushLogCommand());
        reg(MessageType.CMD_ECHO, type -> new EchoCommand());
        reg(MessageType.CMD_SEARCH, MapCommand::new);
        reg(MessageType.CMD_INFO, MapCommand::new);
    }

    private static void reg(int type, Function<Integer, Message> func) {
        typeLock.sync(lock -> typeToCreator.put(type, func));
    }

    private static Function<Integer, Message> getMsgSupplier(int type) {
        return typeLock.syncValue(lock ->
                Optional.ofNullable(typeToCreator.get(type))
                        .orElseThrow(
                                () -> new RuntimeException("Unknown message type: " + type)
                        )
        );
    }

    private static synchronized Message parse(ByteBuffer bb) {
        bb.mark();
        int type = bb.getInt();
        Message message = getMsgSupplier(type).apply(type);
        logger.debug("Message type: {}, version: {}", type, message.getVersion());
        bb.reset();
        message.deserialize(bb);
        return message;
    }

    public static Command parseCommand(ByteBuffer bb) {
        return (Command) parse(bb);
    }

    public static ExecResult parseResult(ByteBuffer bb) {
        return (ExecResult) parse(bb);
    }

}
