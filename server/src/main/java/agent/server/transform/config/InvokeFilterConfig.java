package agent.server.transform.config;

import java.util.List;
import java.util.Objects;

abstract class InvokeFilterConfig extends FilterConfig {
    private List<CallChainConfig> callChainFilters;

    public List<CallChainConfig> getCallChainFilters() {
        return callChainFilters;
    }

    public void setCallChainFilters(List<CallChainConfig> callChainFilters) {
        this.callChainFilters = callChainFilters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InvokeFilterConfig that = (InvokeFilterConfig) o;
        return Objects.equals(callChainFilters, that.callChainFilters);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), callChainFilters);
    }

    @Override
    public String toString() {
        return "InvokeFilterConfig{" +
                "callChainFilters=" + callChainFilters + ", " +
                super.toString() +
                '}';
    }
}
