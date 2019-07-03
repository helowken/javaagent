package agent.common.message.result;

import agent.common.message.AbstractMessage;
import agent.common.struct.impl.MapStruct;

public abstract class AbstractExecResult extends AbstractMessage<MapStruct<String, Object>> implements ExecResult {
    private static final String FIELD_CMD_TYPE = "cmdType";
    private static final String FIELD_STATUS = "status";

    AbstractExecResult() {
        super(ResultType.RS_TYPE_DEFAULT, new MapStruct<>());
    }

    AbstractExecResult(ResultStatus resultStatus, int cmdType) {
        this();
        getBody().put(FIELD_CMD_TYPE, cmdType);
        getBody().put(FIELD_STATUS, resultStatus.status);
    }

    @Override
    public ResultStatus getStatus() {
        return ResultStatus.parse((Byte) getBody().get(FIELD_STATUS));
    }
}
