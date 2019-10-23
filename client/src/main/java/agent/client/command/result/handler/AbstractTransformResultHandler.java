package agent.client.command.result.handler;

import agent.base.utils.IndentUtils;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.TransformResultEntity;
import agent.common.utils.JSONUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

import static agent.common.message.result.entity.TransformResultEntity.*;

abstract class AbstractTransformResultHandler extends AbstractContextResultHandler {

    void handleFailResult(ExecResult result, String msgPrefix) {
        List<TransformResultEntity> rsList = JSONUtils.convert(result.getContent(),
                new TypeReference<List<TransformResultEntity>>() {
                }
        );
        write(msgPrefix + " Result",
                rsList,
                TransformResultEntity::getContext,
                (sb, entity) -> {
                    if (entity.hasError()) {
                        entity.getTypeToErrorList().forEach(
                                (type, errorList) -> {
                                    sb.append(
                                            IndentUtils.getIndent(1)
                                    ).append(
                                            getTypeErrorMsg(type)
                                    ).append(":\n");
                                    errorList.forEach(
                                            errorMsg -> sb.append(
                                                    IndentUtils.getIndent(2)
                                            ).append(
                                                    errorMsg
                                            ).append("\n")
                                    );
                                }
                        );
                    } else
                        sb.append(IndentUtils.getIndent(1)).append("Transform successfully.\n");
                }
        );
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
