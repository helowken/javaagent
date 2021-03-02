package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.StringParser;
import agent.base.utils.Utils;
import agent.client.ClientMgr;
import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.cmdline.help.HelpArg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandFileCmdParser extends ClientAbstractCmdParser<CmdParams> {
    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.DEFAULT;
    }

    @Override
    protected List<HelpArg> createHelpArgList() {
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
    protected Command createCommand(CmdParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CmdItem> parse(String[] args) {
        CmdParams params = doParse(args);
        String filePath = FileUtils.getAbsolutePath(
                params.getArgs()[0],
                true
        );
        List<CmdItem> rsList = new ArrayList<>();
        Utils.wrapToRtError(
                () -> IOUtils.read(
                        filePath,
                        reader -> {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                if (Utils.isNotBlank(line) &&
                                        !line.startsWith("#") &&
                                        !line.startsWith("//")) {
                                    List<String> argList = splitStringToArgs(line);
                                    List<CmdItem> itemList = ClientMgr.getCmdRunner().getCmdParseMgr().parse(argList);
                                    for (CmdItem item : itemList) {
                                        item.setCmdLine(line);
                                    }
                                    rsList.addAll(itemList);
                                }
                            }
                        }
                )
        );
        return rsList;
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
