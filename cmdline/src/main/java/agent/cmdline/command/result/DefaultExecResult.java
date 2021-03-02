package agent.cmdline.command.result;

public class DefaultExecResult implements ExecResult {
    private int cmdType;
    private byte status;
    private String message;
    private Object content;

    public static DefaultExecResult toSuccess(int cmdType) {
        return toSuccess(cmdType, null);
    }

    public static DefaultExecResult toSuccess(int cmdType, String msg) {
        return toSuccess(cmdType, msg, null);
    }

    public static DefaultExecResult toSuccess(int cmdType, String msg, Object content) {
        return toResult(cmdType, SUCCESS, msg, content);
    }

    public static DefaultExecResult toError(int cmdType, String msg, Object content) {
        return toResult(cmdType, ERROR, msg, content);
    }

    public static DefaultExecResult toRuntimeError(String errMsg) {
        return toError(-1, errMsg, null);
    }

    private static DefaultExecResult toResult(int cmdType, byte status, String msg, Object content) {
        DefaultExecResult entity = new DefaultExecResult();
        entity.setCmdType(cmdType);
        entity.setStatus(status);
        entity.setMessage(msg);
        entity.setContent(content);
        return entity;
    }

    @Override
    public int getCmdType() {
        return cmdType;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }

    @Override
    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public <T> T getContent() {
        return (T) content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "DefaultExecResult{" +
                "cmdType=" + cmdType +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", content=" + content +
                '}';
    }
}
