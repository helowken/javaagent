package agent.server.transform;

import agent.base.utils.Utils;

import java.util.concurrent.Callable;

public class TransformLock {
    public static synchronized <T> T useLock(Callable<T> callable) {
        return Utils.wrapToRtError(callable::call);
    }
}
