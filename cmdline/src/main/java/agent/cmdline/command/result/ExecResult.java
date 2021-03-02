package agent.cmdline.command.result;

public interface ExecResult {
    byte SUCCESS = 0;
    byte ERROR = 1;

    byte getStatus();

    default boolean isSuccess() {
        return getStatus() == SUCCESS;
    }

    default boolean isError() {
        return getStatus() == ERROR;
    }

    int getCmdType();

    String getMessage();

    <T> T getContent();
}
