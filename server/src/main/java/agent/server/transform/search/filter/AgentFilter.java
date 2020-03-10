package agent.server.transform.search.filter;

public interface AgentFilter<T> {
    boolean accept(T v);
}
