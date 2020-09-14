package agent.server.command.entity;

import java.util.List;

public class StackTraceEntity {
    private long threadId;
    private String threadName;
    private List<StackTraceElementEntity> stackTraceElements;

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public List<StackTraceElementEntity> getStackTraceElements() {
        return stackTraceElements;
    }

    public void setStackTraceElements(List<StackTraceElementEntity> stackTraceElements) {
        this.stackTraceElements = stackTraceElements;
    }
}
