package agent.client;

import agent.base.utils.IOUtils;
import agent.client.utils.ClientLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AgentClientRunner extends AbstractClientRunner {
    private static BufferedReader reader;
    private static final AgentClientRunner instance = new AgentClientRunner();

    private AgentClientRunner() {
    }

    public static void run() throws Exception {
        init();
        try {
            while (true) {
                if (instance.connectTo())
                    break;
                else
                    ClientLogger.logger.info("Try to reconnect...");
            }
        } finally {
            IOUtils.close(reader);
        }
    }

    public static void shutdown() {
    }

    private static void init() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    String readCmdLine() throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty())
                return line;
        }
        return null;
    }
}
