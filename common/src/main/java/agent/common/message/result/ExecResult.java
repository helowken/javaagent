package agent.common.message.result;

import agent.common.message.Message;

public interface ExecResult extends Message {
    ResultStatus getStatus();

    String getMessage();

    <T> T getContent();
}
