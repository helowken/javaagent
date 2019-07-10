package agent.client.command.result.handler;

import agent.base.utils.IndentUtils;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.TransformResultEntity;
import agent.common.utils.JSONUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

abstract class AbstractTransformResultHandler extends AbstractContextResultHandler {

    void handleFailResult(ExecResult result, String msgPrefix) throws Exception {
        List<TransformResultEntity> rsList = JSONUtils.convert(result.getContent(),
                new TypeReference<List<TransformResultEntity>>() {
                }
        );
        write(msgPrefix + " Result",
                rsList,
                TransformResultEntity::getContext,
                (sb, entity) -> {
                    if (entity.getInstrumentError() != null)
                        sb.append("Instrument error: ").append(entity.getInstrumentError());
                    entity.getTransformerToError().forEach((key, error) ->
                            sb.append(IndentUtils.getIndent(1)).append("Transformer: ").append(key).append("\n")
                                    .append(IndentUtils.getIndent(1)).append("Error: ").append(error).append("\n")
                    );
                }
        );
    }
}
