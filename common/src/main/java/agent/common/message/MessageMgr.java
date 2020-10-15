package agent.common.message;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.message.command.impl.MapCommand;
import agent.common.message.command.impl.StringCommand;
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
        reg(
                type -> new DefaultExecResult(),
                RESULT_DEFAULT
        );

        reg(
                StringCommand::new,
                CMD_FLUSH_LOG,
                CMD_ECHO,
                CMD_JS_CONFIG
        );

        reg(
                MapCommand::new,
                CMD_RESET,
                CMD_TRANSFORM,
                CMD_SEARCH,
                CMD_INFO,
                CMD_SAVE_CLASS,
                CMD_STACK_TRACE
        );
    }

    private static void reg(Function<Integer, Message> func, int... types) {
        if (types == null)
            throw new IllegalArgumentException();
        for (int type : types) {
            typeLock.sync(lock -> typeToCreator.put(type, func));
        }
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
