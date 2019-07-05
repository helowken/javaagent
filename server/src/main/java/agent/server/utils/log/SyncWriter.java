package agent.server.utils.log;

import agent.base.utils.LockObject;

public class SyncWriter {
    private final OutputWriter outputWriter;
    private final LockObject lock = new LockObject();

    SyncWriter(OutputWriter outputWriter) {
        this.outputWriter = outputWriter;
    }

    public void exec(OutputFunc func) {
        lock.sync(lo -> func.exec(outputWriter));
    }
}
