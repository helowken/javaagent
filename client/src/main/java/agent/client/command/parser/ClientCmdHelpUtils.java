package agent.client.command.parser;

import agent.cmdline.help.HelpArg;

import java.util.Collections;
import java.util.Map;

public class ClientCmdHelpUtils {

    public static HelpArg getTransformerIdHelpArg() {
        return new HelpArg(
                "TID",
                "Unique id to identify transformer."
        );
    }

    public static HelpArg getOutputPathHelpArg(boolean isOptional) {
        return new HelpArg(
                "OUTPUT_PATH",
                "File path used to store data.",
                isOptional
        );
    }

    public static Map<String, Object> newLogConfig(String outputPath) {
        return Collections.singletonMap(
                "outputPath",
                outputPath
        );
    }
}
