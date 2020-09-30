package agent.bootstrap;

public class ProxyDelegate {
    private static final ProxyDelegate instance = new ProxyDelegate();
    private ProxyIntf proxy;

    public static ProxyDelegate getInstance() {
        return instance;
    }

    public void setProxy(ProxyIntf v) {
        proxy = v;
    }

    public void onBefore(int invokeId, Object instanceOrNull, Object[] args) {
        if (proxy != null)
            proxy.onBefore(invokeId, instanceOrNull, args);
    }

    public void onReturning(int invokeId, Object instanceOrNull, Object returnValue) {
        if (proxy != null)
            proxy.onReturning(invokeId, instanceOrNull, returnValue);
    }

    public void onThrowing(int invokeId, Object instanceOrNull, Throwable error) {
        if (proxy != null)
            proxy.onThrowing(invokeId, instanceOrNull, error);
    }

    public void onCatching(int invokeId, Object instanceOrNull, Throwable error) {
        if (proxy != null)
            proxy.onCatching(invokeId, instanceOrNull, error);
    }
}
