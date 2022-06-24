package agent.builtin.tools.config;

public class ConsumedTimeResultConfig extends AbstractResultConfig {
    private boolean invoke;
    private boolean readCache;

    public boolean isInvoke() {
        return invoke;
    }

    public void setInvoke(boolean invoke) {
        this.invoke = invoke;
    }

    public boolean isReadCache() {
        return readCache;
    }

    public void setReadCache(boolean readCache) {
        this.readCache = readCache;
    }
}
