package agent.common.message;

import agent.common.message.version.ApiVersion;

public interface Message {
    int getType();

    ApiVersion getApiVersion();

    <T> T getBody();
}
