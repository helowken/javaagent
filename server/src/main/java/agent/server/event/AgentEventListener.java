package agent.server.event;

public interface AgentEventListener {
    void onNotify(AgentEvent event);

    boolean accept(AgentEvent event);
}
