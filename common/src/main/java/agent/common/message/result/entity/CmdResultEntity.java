package agent.common.message.result.entity;

import agent.common.message.result.ExecResult;
import agent.common.struct.impl.annotation.PojoProperty;

public class CmdResultEntity implements ExecResult {
    @PojoProperty(index = 0)
    private int cmdType;
    @PojoProperty(index = 1)
    private byte status;
    @PojoProperty(index = 2)
    private String message;
    @PojoProperty(index = 3)
    private Object content;

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
}
