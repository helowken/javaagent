package agent.common.message.result;

import agent.common.message.AbstractMessage;
import agent.common.message.MessageType;
import agent.common.struct.impl.MapStruct;

@SuppressWarnings("unchecked")
public class DefaultExecResult extends AbstractMessage<MapStruct<String, Object>> implements ExecResult {
    private static final String FIELD_CMD_TYPE = "cmdType";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_MESSAGE = "message";

    public static DefaultExecResult toSuccess(int cmdType) {
        return toSuccess(cmdType, null);
    }

    public static DefaultExecResult toSuccess(int cmdType, String msg) {
        return toSuccess(cmdType, msg, null);
    }

    public static DefaultExecResult toSuccess(int cmdType, String msg, Object content) {
        return toResult(ResultStatus.SUCCESS, cmdType, msg, content);
    }

    public static DefaultExecResult toError(int cmdType, String errorMsg, Object content) {
        return toResult(ResultStatus.ERROR, cmdType, errorMsg, content);
    }

    public static DefaultExecResult toRuntimeError(String errorMsg) {
        return toError(MessageType.CMD_NONE, errorMsg, null);
    }

    public static DefaultExecResult toResult(ResultStatus resultStatus, int messageType, String msg, Object content) {
        return new DefaultExecResult(resultStatus, messageType, msg, content);
    }

    public DefaultExecResult() {
        super(MessageType.RESULT_DEFAULT, new MapStruct<>());
    }

    private DefaultExecResult(ResultStatus resultStatus, int cmdType, String msg, Object content) {
        this();
        getBody().put(FIELD_CMD_TYPE, cmdType);
        getBody().put(FIELD_STATUS, resultStatus.status);
        getBody().put(FIELD_MESSAGE, msg);
        getBody().put(FIELD_CONTENT, content);
    }

    @Override
    public ResultStatus getStatus() {
        return ResultStatus.parse((Byte) getBody().get(FIELD_STATUS));
    }

    @Override
    public int getCmdType() {
        return (int) getBody().get(FIELD_CMD_TYPE);
    }

    @Override
    public String getMessage() {
        return (String) getBody().get(FIELD_MESSAGE);
    }

    public <T> T getContent() {
        return (T) getBody().get(FIELD_CONTENT);
    }

}
