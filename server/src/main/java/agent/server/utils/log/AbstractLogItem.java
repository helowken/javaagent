package agent.server.utils.log;

public abstract class AbstractLogItem implements LogItem {
    private Runnable postWriteFunc;

    public void setPostWriteFunc(Runnable func) {
        postWriteFunc = func;
    }

    @Override
    public void postWrite() {
        if (postWriteFunc != null)
            postWriteFunc.run();
    }
}
