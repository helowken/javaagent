package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.help.HelpArg;
import agent.base.utils.*;
import agent.client.AgentClientRunner;
import agent.client.args.parse.DefaultParamParser;
import agent.client.command.parser.exception.CommandParseException;
import agent.common.message.command.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static agent.common.args.parse.FilterOptUtils.getHelpOptParser;

public class CommandFileCmdParser extends AbstractCmdParser<CmdParams> {
    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                Collections.singletonList(
                        getHelpOptParser()
                )
        );
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "FILE_PATH",
                        "File contains one or more commands to be executed."
                )
        );
    }

    private List<String> splitStringToArgs(String line) {
        StringParser.CompiledStringExpr expr = StringParser.compile(line, "\"", "\"");
        List<String> rsList = new ArrayList<>();
        expr.getAllItems().forEach(
                item -> {
                    String content = item.getContent().trim();
                    if (item.isKey())
                        rsList.add(content);
                    else
                        Collections.addAll(
                                rsList,
                                content.split("\\s+")
                        );
                }
        );
        return rsList;
    }

    @Override
    Command createCommand(CmdParams params) {
        String filePath = FileUtils.getAbsolutePath(
                params.getArgs()[0]
        );
        Utils.wrapToRtError(
                () -> IOUtils.read(
                        filePath,
                        reader -> {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (Utils.isNotBlank(line)) {
                                    List<String> args = splitStringToArgs(line);
                                    if (args.isEmpty())
                                        throw new CommandParseException("Invalid command: " + line);
                                    ConsoleLogger.getInstance().info("\nExec cmd: {}", line);
                                    boolean v = AgentClientRunner.getInstance().execCmd(args);
                                    if (!v)
                                        break;
                                }
                            }
                        }
                )
        );
        return null;
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"file"};
    }

    @Override
    public String getDesc() {
        return "Run commands in a file.";
    }
}
