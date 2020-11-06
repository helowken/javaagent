package agent.common.message.command;

public interface Command {
    int getType();

    <T> T getContent();
}
