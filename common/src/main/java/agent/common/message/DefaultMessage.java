package agent.common.message;

import agent.common.message.version.ApiVersion;
import agent.common.struct.impl.annotation.PojoProperty;

public class DefaultMessage implements Message {
    @PojoProperty(index = 1)
    private ApiVersion apiVersion;
    @PojoProperty(index = 2)
    private Object body;

    public static Message toMessage(Object body) {
        DefaultMessage message = new DefaultMessage();
        message.setApiVersion(ApiVersion.getDefault());
        message.setBody(body);
        return message;
    }

    public ApiVersion getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(ApiVersion apiVersion) {
        this.apiVersion = apiVersion;
    }

    public <T> T getBody() {
        return (T) body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
