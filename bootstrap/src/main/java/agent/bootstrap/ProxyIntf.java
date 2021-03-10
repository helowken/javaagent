package agent.bootstrap;

public interface ProxyIntf {
    void onBefore(int invokeId, Object instanceOrNull, Object[] args);

    void onReturning(int invokeId, Object instanceOrNull, Object returnValue);

    void onThrowingNotCatch(int invokeId, Object instanceOrNull, Throwable error);

    void onThrowing(int invokeId, Object instanceOrNull, Throwable error);

    void onCatching(int invokeId, Object instanceOrNull, Throwable error);
}
