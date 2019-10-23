package agent.common.message.result;

import agent.common.message.Message;

public interface ExecResult extends Message {
    ResultStatus getStatus();

    int getCmdType();

    String getMessage();

    <T> T getContent();
}
