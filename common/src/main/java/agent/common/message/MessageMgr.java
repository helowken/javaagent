package agent.common.message;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.cmdline.command.DefaultCommand;
import agent.common.config.*;
import agent.cmdline.command.result.DefaultExecResult;
import agent.common.message.result.entity.ErrorEntity;
import agent.common.message.result.entity.TransformResultEntity;
import agent.common.message.version.ApiVersion;
import agent.common.struct.impl.PojoFieldPropertyList;
import agent.common.struct.impl.PojoInfo;
import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructContext;

import java.nio.ByteBuffer;

public class MessageMgr {
    private static final Logger logger = Logger.getLogger(MessageMgr.class);
    private static final StructContext context = new StructContext();

    static {
        Class<?>[] pojoClasses = new Class[]{
                ApiVersion.class,
                DefaultMessage.class,

                DefaultExecResult.class,
                ErrorEntity.class,
                TransformResultEntity.class,

                StringFilterConfig.class,
                ClassFilterConfig.class,
                MethodFilterConfig.class,
                ConstructorFilterConfig.class,
                InvokeChainConfig.class,
                TargetConfig.class,
                TransformerConfig.class,
                ModuleConfig.class,

                InfoQuery.class,
                ResetConfig.class,
                SaveClassConfig.class,
                StackTraceConfig.class
        };

        int pojoType = 0;
        for (Class<?> pojoClass : pojoClasses) {
            context.addPojoInfo(pojoClass, pojoType++);
        }

        context.addPojoInfo(
                DefaultCommand.class,
                new PojoInfo<>(
                        pojoType,
                        DefaultCommand::new,
                        PojoFieldPropertyList.create(
                                DefaultCommand.class,
                                DefaultCommand.getFieldNames()
                        )
                )
        );
    }

    public static Message deserialize(ByteBuffer bb) {
        return Utils.wrapToRtError(
                () -> {
                    Message message = Struct.deserialize(bb, context);
                    logger.debug("Deserialize message version: {}", message.getApiVersion());
                    return message;
                }
        );
    }

    public static ByteBuffer serialize(Message message) {
        return Utils.wrapToRtError(
                () -> {
                    logger.debug("Serialize message version: {}", message.getApiVersion());
                    return Struct.serialize(message, context);
                }
        );
    }
}
