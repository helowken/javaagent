package agent.launcher.client;


import agent.base.parser.BasicParams;

import java.util.ArrayList;
import java.util.List;

class ClientParams extends BasicParams<ClientOptions> {
    String configFilePath;
    List<Object> cmdArgs = new ArrayList<>();
}
