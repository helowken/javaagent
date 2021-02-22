package agent.client.command.parser;

import agent.client.command.parser.impl.*;
import agent.cmdline.command.parser.AbstractCommandParserMgr;

import java.util.Arrays;
import java.util.Collections;

public class ClientCommandParserMgr extends AbstractCommandParserMgr {
    private static final ClientCommandParserMgr instance = new ClientCommandParserMgr();

    public static ClientCommandParserMgr getInstance() {
        return instance;
    }

    private ClientCommandParserMgr() {
        reg(
                "",
                Collections.singletonList(
                        new ClientHelpCmdParser()
                )
        );
        reg(
                "System Management:",
                Arrays.asList(
                        new InfoCmdParser(),
                        new SearchCmdParser(),
                        new ResetCmdParser(),
                        new FlushLogCmdParser(),
                        new CommandFileCmdParser(),
                        new EchoCmdParser()
                )
        );
        reg(
                "Service Management:",
                Arrays.asList(
                        new BuiltInTransformCmdParser.CostTimeCmdParser(),
                        new BuiltInTransformCmdParser.TraceCmdParser(),
                        new JavascriptTransformCmdParser(),
                        new JavascriptConfigCmdParser(),
                        new JavascriptExecCmdParser(),
                        new StackTraceCmdParser(),
                        new SaveClassCmdParser()
                )
        );
    }

}
