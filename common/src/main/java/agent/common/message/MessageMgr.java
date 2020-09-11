package agent.common.message;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.message.command.impl.EchoCommand;
import agent.common.message.command.impl.FlushLogCommand;
import agent.common.message.command.impl.MapCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.struct.DefaultBBuff;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static agent.common.message.MessageType.*;

@SuppressWarnings("unchecked")
public class MessageMgr {
    private static final Logger logger = Logger.getLogger(MessageMgr.class);
    private static final Map<Integer, Function<Integer, Message>> typeToCreator = new HashMap<>();
    private static final LockObject typeLock = new LockObject();

    static {
        reg(RESULT_DEFAULT, type -> new DefaultExecResult());
        reg(CMD_FLUSH_LOG, type -> new FlushLogCommand());
        reg(CMD_ECHO, type -> new EchoCommand());

        int[] types = new int[]{
                CMD_RESET,
                CMD_TRANSFORM,
                CMD_SEARCH,
                CMD_INFO,
                CMD_SAVE_CLASS
        };
        for (int type : types) {
            reg(type, MapCommand::new);
        }
    }

    private static void reg(int type, Function<Integer, Message> func) {
        typeLock.sync(lock -> typeToCreator.put(type, func));
    }

    private static Function<Integer, Message> getMsgSupplier(int type) {
        return typeLock.syncValue(lock ->
                Optional.ofNullable(
                        typeToCreator.get(type)
                ).orElseThrow(
                        () -> new RuntimeException("Unknown message type: " + type)
                )
        );
    }

    private static synchronized Message doParse(ByteBuffer bb) {
        bb.mark();
        int type = bb.getInt();
        Message message = getMsgSupplier(type).apply(type);
        logger.debug("Message type: {}, version: {}", type, message.getVersion());
        bb.reset();
        message.deserialize(
                new DefaultBBuff(bb)
        );
        return message;
    }

    public static <T extends Message> T parse(ByteBuffer bb) {
        return (T) doParse(bb);
    }
}
