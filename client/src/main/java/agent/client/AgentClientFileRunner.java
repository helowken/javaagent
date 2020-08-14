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
        String[] restArgs = init(args);
        String filePath = Utils.getArgValue(restArgs, 1);
        ConsoleLogger.getInstance().info("Execute script file: {}", new File(filePath).getCanonicalPath());
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (Exception e) {
            ConsoleLogger.getInstance().error("Failed. Error Cause: {}", e.getMessage());
            return;
        }
        execCmd();
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
