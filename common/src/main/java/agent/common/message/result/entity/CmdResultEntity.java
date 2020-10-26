package agent.common.message.result.entity;

import agent.common.utils.annotation.PojoProperty;

public class CmdResultEntity {
    @PojoProperty(index = 0)
    private int cmdType;
    @PojoProperty(index = 1)
    private byte status;
    @PojoProperty(index = 2)
    private String message;
    @PojoProperty(index = 3)
    private Object content;

    public int getCmdType() {
        return cmdType;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
