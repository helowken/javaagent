package agent.base.utils;

public class LockObject {
    private final Object lock = new Object();

    public void sync(SyncVoidFunc func) {
        synchronized (lock) {
            Utils.wrapToRtError(() -> func.exec(lock));
        }
    }

    public <T> T syncValue(SyncValueFunc<T> func) {
        synchronized (lock) {
            return Utils.wrapToRtError(() -> func.exec(lock));
        }
    }

    public void syncAndNotify(SyncVoidFunc func) {
        sync(lock -> {
            func.exec(lock);
            lock.notify();
        });
    }

    public void syncAndNotifyAll(SyncVoidFunc func) {
        sync(lock -> {
            func.exec(lock);
            lock.notifyAll();
        });
    }

    public interface SyncVoidFunc {
        void exec(Object lock) throws Exception;
    }

    public interface SyncValueFunc<T> {
        T exec(Object lock) throws Exception;
    }
}
