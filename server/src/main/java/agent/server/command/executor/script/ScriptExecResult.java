package agent.server.command.executor.script;

public class ScriptExecResult<T> {
    public final T value;
    public final boolean hasContent;
    public final String content;
    public final boolean hasError;
    public final String errorContent;

    public ScriptExecResult(T value, boolean hasContent, String content, boolean hasError, String errorContent) {
        this.value = value;
        this.hasContent = hasContent;
        this.content = content;
        this.hasError = hasError;
        this.errorContent = errorContent;
    }
}
