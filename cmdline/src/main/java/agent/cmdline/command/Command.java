package agent.cmdline.command;

public interface Command {
    int getType();

    <T> T getContent();
}
