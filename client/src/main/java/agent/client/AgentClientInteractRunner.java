package agent.client;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class AgentClientInteractRunner extends AbstractClientRunner {
    private BufferedReader reader;

    @Override
    public void startup(Object... args) {
        init(args);
        reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                if (connectTo())
                    break;
                else
                    ConsoleLogger.getInstance().info("Try to reconnect...");
            }
        } finally {
            IOUtils.close(reader);
        }
    }

    @Override
    List<String> readCmdArgs() throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty())
                return splitStringToArgs(line);
        }
        return null;
    }
}
