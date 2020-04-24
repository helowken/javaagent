package agent.launcher.client;

import static agent.launcher.client.ClientArgsCmdParser.RUNNER_TYPE_INTERACT;

public class ClientOptions {
    public String host = "127.0.0.1";
    public int port = 10086;
    public String runnerType = RUNNER_TYPE_INTERACT;
}
