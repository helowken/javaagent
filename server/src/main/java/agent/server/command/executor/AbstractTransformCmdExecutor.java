package agent.server.command.executor;

import agent.base.utils.Utils;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.ErrorEntity;
import agent.common.message.result.entity.TransformResultEntity;
import agent.common.utils.JSONUtils;
import agent.server.transform.TransformResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class AbstractTransformCmdExecutor extends AbstractCmdExecutor {
    ExecResult convert(List<TransformResult> transformResultList, int cmdType, String msgPrefix) {
        List<TransformResultEntity> entityList = transformResultList.stream()
                .filter(TransformResult::hasError)
                .map(transformResult -> {
                    TransformResultEntity entity = new TransformResultEntity();
                    entity.setContext(transformResult.transformContext.context);
                    if (transformResult.instrumentError != null) {
                        entity.setInstrumentError(
                                new ErrorEntity(Utils.getErrorMessages(transformResult.instrumentError))
                        );
                    }
                    transformResult.getTransformerToError().forEach((key, error) ->
                            entity.addTransformerError(key,
                                    new ErrorEntity(Utils.getErrorMessages(error))
                            )
                    );
                    return entity;
                })
                .collect(Collectors.toList());
        return entityList.isEmpty() ?
                DefaultExecResult.toSuccess(cmdType, msgPrefix + " successfully.") :
                DefaultExecResult.toError(cmdType, msgPrefix + " failed.",
                        JSONUtils.convert(entityList,
                                new TypeReference<List<Map>>() {
                                }
                        )
                );
    }
}
