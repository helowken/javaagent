package agent.common.message;

import agent.common.message.result.ExecResult;
import agent.common.message.version.ApiVersion;
import agent.common.struct.impl.annotation.PojoProperty;

import static agent.common.message.MessageType.RESULT_DEFAULT;

public class DefaultMessage implements Message {
    @PojoProperty(index = 0)
    private int type;
    @PojoProperty(index = 1)
    private ApiVersion apiVersion;
    @PojoProperty(index = 2)
    private Object body;

    public static Message toResult(ExecResult result) {
        return toMessage(RESULT_DEFAULT, result);
    }

    public static Message toMessage(int type, Object body) {
        DefaultMessage message = new DefaultMessage();
        message.setApiVersion(ApiVersion.getDefault());
        message.setType(type);
        message.setBody(body);
        return message;
    }

    public ApiVersion getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(ApiVersion apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public <T> T getBody() {
        return (T) body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
