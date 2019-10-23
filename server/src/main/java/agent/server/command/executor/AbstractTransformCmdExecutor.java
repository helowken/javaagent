package agent.server.command.executor;

import agent.base.utils.Utils;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.ErrorEntity;
import agent.common.message.result.entity.TransformResultEntity;
import agent.common.utils.JSONUtils;
import agent.server.transform.AgentTransformer;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.TransformResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

abstract class AbstractTransformCmdExecutor extends AbstractCmdExecutor {
    ExecResult convert(List<TransformResult> transformResultList, final int cmdType, String msgPrefix) {
        AtomicBoolean failed = new AtomicBoolean(false);
        List<TransformResultEntity> entityList = transformResultList.stream()
                .map(result -> {
                    if (result.hasError())
                        failed.set(true);
                    TransformResultEntity entity = new TransformResultEntity();
                    entity.setContext(result.getTransformContext().context);
                    if (result.hasTransformError())
                        addErrorList(entity, TransformResultEntity.TRANSFORM_ERROR, result.getTransformErrorList());
                    if (result.hasCompileError())
                        addErrorList(entity, TransformResultEntity.COMPILE_ERROR, result.getCompileErrorList());
                    if (result.hasReTransformError())
                        addErrorList(entity, TransformResultEntity.RETRANSFORM_ERROR, result.getReTransformErrorItemList());
                    return entity;
                })
                .collect(Collectors.toList());
        return failed.get() ?
                DefaultExecResult.toError(cmdType, msgPrefix + " failed.",
                        JSONUtils.convert(entityList,
                                new TypeReference<List<Map>>() {
                                }
                        )
                ) :
                DefaultExecResult.toSuccess(cmdType, msgPrefix + " successfully.");
    }

    private <T extends TransformResult.ErrorItem> void addErrorList(TransformResultEntity resultEntity, int type, List<T> errorItemList) {
        errorItemList.forEach(
                errorItem -> resultEntity.addError(
                        type,
                        new ErrorEntity(
                                errorItem.clazz.getName(),
                                getTransformerKey(errorItem.transformer),
                                Utils.getMergedErrorMessage(errorItem.error)
                        )
                )
        );
    }

    private String getTransformerKey(AgentTransformer transformer) {
        if (transformer instanceof ConfigTransformer)
            return ((ConfigTransformer) transformer).getRegKey();
        return null;
    }
}
