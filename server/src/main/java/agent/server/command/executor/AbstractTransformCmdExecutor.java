package agent.server.command.executor;

import agent.base.utils.Utils;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.DefaultExecResult;
import agent.common.message.result.entity.ErrorEntity;
import agent.common.message.result.entity.TransformResultEntity;
import agent.server.transform.TransformResult;

import java.util.List;

abstract class AbstractTransformCmdExecutor extends AbstractCmdExecutor {
    ExecResult convert(TransformResult result, final int cmdType, String msgPrefix) {
        boolean failed = false;
        if (result.hasError())
            failed = true;
        TransformResultEntity entity = new TransformResultEntity();
        if (result.hasTransformError())
            addErrorList(
                    entity,
                    TransformResultEntity.TRANSFORM_ERROR,
                    result.getTransformErrorList()
            );
        if (result.hasCompileError())
            addErrorList(
                    entity,
                    TransformResultEntity.COMPILE_ERROR,
                    result.getCompileErrorList()
            );
        if (result.hasReTransformError())
            addErrorList(
                    entity,
                    TransformResultEntity.RETRANSFORM_ERROR,
                    result.getReTransformErrorItemList()
            );

        return failed ?
                DefaultExecResult.toError(cmdType, msgPrefix + " failed.", entity) :
                DefaultExecResult.toSuccess(cmdType, msgPrefix + " successfully.");
    }

    private <T extends TransformResult.ErrorItem> void addErrorList(TransformResultEntity resultEntity, int type, List<T> errorItemList) {
        errorItemList.forEach(
                errorItem -> resultEntity.addError(
                        type,
                        new ErrorEntity(
                                errorItem.getTargetClassName(),
                                errorItem.getTransformerKey(),
                                Utils.getMergedErrorMessage(
                                        errorItem.getError()
                                )
                        )
                )
        );
    }
}
