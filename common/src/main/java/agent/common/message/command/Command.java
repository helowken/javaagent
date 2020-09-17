package agent.common.message.command;

import agent.common.message.Message;

import java.util.Collections;
import java.util.List;

public interface Command extends Message {
    default List<Command> getCommands() {
        return Collections.singletonList(this);
    }
}
