package agent.common.message;

import agent.common.message.version.ApiVersion;

public interface Message {
    ApiVersion getApiVersion();

    <T> T getBody();
}
