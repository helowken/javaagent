package agent.client;

import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.client.utils.ClientLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class AgentScriptRunner extends AbstractClientRunner {
    private BufferedReader reader;

    @Override
    public void startup(Object... args) throws Exception {
        String filePath = Utils.getArgValue(args, 0);
        ClientLogger.info("Execute script file: " + new File(filePath).getCanonicalPath());
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (Exception e) {
            ClientLogger.error("Failed. Error Cause: " + e.getMessage());
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
    String readCmdLine() {
        return Utils.wrapToRtError(
                () -> {
                    String line = readLineFromFile();
                    if (line == null)
                        IOUtils.close(reader);
                    else
                        ClientLogger.info(line);
                    return line;
                }
        );
    }
}
