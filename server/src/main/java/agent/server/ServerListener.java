package agent.server;

public interface ServerListener {
    void onStartup(Object[] args);

    void onShutdown();
}
