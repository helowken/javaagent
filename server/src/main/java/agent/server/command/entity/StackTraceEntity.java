package agent.server.command.entity;

import agent.common.utils.annotation.PojoProperty;

import java.util.List;

public class StackTraceEntity {
    @PojoProperty(index = 0)
    private long threadId;
    @PojoProperty(index = 1)
    private String threadName;
    @PojoProperty(index = 2)
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
