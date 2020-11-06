package agent.common.message.result;

import agent.common.message.MessageType;
import agent.common.message.result.entity.CmdResultEntity;

public class DefaultExecResult {
//    public static DefaultExecResult toSuccess(int cmdType) {
//        return toSuccess(cmdType, null);
//    }
//
//    public static DefaultExecResult toSuccess(int cmdType, String msg) {
//        return toSuccess(cmdType, msg, null);
//    }
//
//    public static DefaultExecResult toSuccess(int cmdType, String msg, Object content) {
//        return toResult(ResultStatus.SUCCESS, cmdType, msg, content);
//    }
//
//    public static DefaultExecResult toError(int cmdType, String errorMsg, Object content) {
//        return toResult(ResultStatus.ERROR, cmdType, errorMsg, content);
//    }
//
//    public static DefaultExecResult toRuntimeError(String errorMsg) {
//        return toError(MessageType.CMD_NONE, errorMsg, null);
//    }
//
//    public static DefaultExecResult toResult(ResultStatus resultStatus, int messageType, String msg, Object content) {
//        return new DefaultExecResult(resultStatus, messageType, msg, content);
//    }
//
//    public DefaultExecResult() {
//        super(MessageType.RESULT_DEFAULT, Structs.newPojo());
//        this.getBody().setPojo(new CmdResultEntity());
//    }
//
//    private DefaultExecResult(ResultStatus resultStatus, int cmdType, String msg, Object content) {
//        this();
//        CmdResultEntity entity = getEntity();
//        entity.setCmdType(cmdType);
//        entity.setStatus(resultStatus.status);
//        entity.setMessage(msg);
//        entity.setContent(content);
//    }
//
//    private CmdResultEntity getEntity() {
//        return getBody().getPojo();
//    }
//
//    @Override
//    public ResultStatus getStatus() {
//        return ResultStatus.parse(
//                getEntity().getStatus()
//        );
//    }
//
//    @Override
//    public int getCmdType() {
//        return getEntity().getCmdType();
//    }
//
//    @Override
//    public String getMessage() {
//        return getEntity().getMessage();
//    }
//
//    public <T> T getContent() {
//        return (T) getEntity().getContent();
//    }

}
