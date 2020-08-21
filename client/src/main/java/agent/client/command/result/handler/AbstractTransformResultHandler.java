package agent.client.command.result.handler;

import agent.base.utils.TypeObject;
import agent.base.utils.Utils;
import agent.base.utils.ConsoleLogger;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.ErrorEntity;
import agent.common.message.result.entity.TransformResultEntity;
import agent.common.utils.JSONUtils;

import static agent.base.utils.IndentUtils.INDENT_1;
import static agent.common.message.result.entity.TransformResultEntity.*;

abstract class AbstractTransformResultHandler extends AbstractExecResultHandler {

    void handleFailResult(ExecResult result, String msgPrefix) {
        TransformResultEntity entity = JSONUtils.convert(
                result.getContent(),
                new TypeObject<TransformResultEntity>() {
                }
        );
        if (entity.hasError()) {
            StringBuilder sb = new StringBuilder();
            entity.getTypeToErrorList().forEach(
                    (type, errorEntityList) -> {
                        sb.append(getTypeErrorMsg(type)).append(":\n");
                        int count = 0;
                        for (ErrorEntity errorEntity : errorEntityList) {
                            if (count > 0)
                                sb.append(INDENT_1).append("--------------------------------\n");

                            String targetClassName = errorEntity.getClassName();
                            if (Utils.isNotBlank(targetClassName))
                                sb.append(INDENT_1).append("Class: ").append(targetClassName).append('\n');

                            String transformerKey = errorEntity.getTransformerKey();
                            if (Utils.isNotBlank(transformerKey))
                                sb.append(INDENT_1).append("Transformer: ").append(transformerKey).append('\n');

                            sb.append(INDENT_1).append("Error: ").append(errorEntity.getErrMsg());
                            ++count;
                        }
                    }
            );
            sb.append('\n');
            ConsoleLogger.getInstance().info(msgPrefix + " failed.\n" + sb.toString());
        }
    }

    private String getTypeErrorMsg(int type) {
        switch (type) {
            case TRANSFORM_ERROR:
                return "Transform Error";
            case COMPILE_ERROR:
                return "Compile Error";
            case RETRANSFORM_ERROR:
                return "Retransform Error";
        }
        throw new RuntimeException("Unknown error type: " + type);
    }
}
