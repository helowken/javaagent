package agent.client;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.IOUtils;
import agent.base.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

public class AgentClientFileRunner extends AbstractClientRunner {
    private BufferedReader reader;

    @Override
    public void startup(Object... args) throws Exception {
        int idx = init(args);
        String filePath = Utils.getArgValue(args, idx);
        ConsoleLogger.getInstance().info("Execute script file: {}", new File(filePath).getCanonicalPath());
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (Exception e) {
            ConsoleLogger.getInstance().error("Failed. Error Cause: {}", e.getMessage());
            return;
        }
        connectTo();
    }

    private String readLineFromFile() throws Exception {
        String line = reader.readLine();
        if (line != null && line.trim().isEmpty())
            return readLineFromFile();
        return line;
    }

    @Override
    List<String> readCmdArgs() {
        return Utils.wrapToRtError(
                () -> {
                    String line = readLineFromFile();
                    if (line == null)
                        IOUtils.close(reader);
                    else
                        ConsoleLogger.getInstance().info(line);
                    return splitStringToArgs(line);
                }
        );
    }
}
