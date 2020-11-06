package agent.common.message;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.common.struct.impl.Struct;

import java.nio.ByteBuffer;

public class MessageMgr {
    private static final Logger logger = Logger.getLogger(MessageMgr.class);

    static {
//        PojoStructCache.addFieldTypeConverter(
//                DefaultMessage.class,
//                (pojo, currType, fieldIndex, level, isKey) -> {
//                    if (fieldIndex == 0) {
//                        switch (pojo.getType()) {
//                            case RESULT_DEFAULT:
//                                return CmdResultEntity.class;
//                            case CMD_TRANSFORM:
//                            case CMD_SEARCH:
//                                return ModuleConfig.class;
//                            case CMD_RESET:
//                                return ResetConfig.class;
//                            case CMD_INFO:
//                                return InfoQuery.class;
//                            case CMD_SAVE_CLASS:
//                                return SaveClassConfig.class;
//                            case CMD_STACK_TRACE:
//                                return StackTraceConfig.class;
//                            case CMD_FLUSH_LOG:
//                            case CMD_ECHO:
//                            case CMD_JS_CONFIG:
//                                return String.class;
//                        }
//                    }
//                    return currType;
//                }
//        );
//        PojoStructCache.addFieldTypeConverter(
//                CmdResultEntity.class,
//                (pojo, currType, fieldIndex, level, isKey) -> {
//                    if (fieldIndex == 3) {
//                        switch (pojo.getCmdType()) {
//                            case CMD_TRANSFORM:
//                            case CMD_RESET:
//                                return TransformResultEntity.class;
//                        }
//                    }
//                    return currType;
//                }
//        );
    }

    public static synchronized Message parse(ByteBuffer bb) {
        return Utils.wrapToRtError(
                () -> {
                    Message message = Struct.deserialize(bb);
                    logger.debug("Message type: {}, version: {}", message.getType(), message.getApiVersion());
                    return message;
                }
        );
    }

}
