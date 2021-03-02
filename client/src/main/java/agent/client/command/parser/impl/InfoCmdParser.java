package agent.client.command.parser.impl;

import agent.base.utils.Utils;
import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.cmdline.command.Command;
import agent.cmdline.exception.UnknownArgException;
import agent.cmdline.help.HelpArg;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.InfoQuery;
import agent.cmdline.command.DefaultCommand;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.config.InfoQuery.*;
import static agent.common.message.MessageType.CMD_INFO;

public class InfoCmdParser extends ClientAbstractCmdParser<CmdParams> {
    private static final String CATALOG_CLASS = "class";
    private static final String CATALOG_INVOKE = "invoke";
    private static final String CATALOG_PROXY = "proxy";
    private static final Map<String, Integer> catalogMap = new LinkedHashMap<>();

    static {
        catalogMap.put(CATALOG_CLASS, INFO_CLASS);
        catalogMap.put(CATALOG_INVOKE, INFO_INVOKE);
        catalogMap.put(CATALOG_PROXY, INFO_PROXY);
    }

    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                getFilterOptParsers()
        );
    }

    @Override
    protected void checkParams(CmdParams params) throws Exception {
        super.checkParams(params);
        String[] args = params.getArgs();
        if (args.length > 0 && !catalogMap.containsKey(args[0]))
            throw new UnknownArgException(args[0]);
    }

    @Override
    protected Command createCommand(CmdParams params) {
        InfoQuery infoQuery = new InfoQuery();
        infoQuery.setTargetConfig(
                FilterOptUtils.createTargetConfig(
                        params.getOpts()
                )
        );
        String[] args = params.getArgs();
        infoQuery.setLevel(
                catalogMap.get(
                        args.length > 0 ? args[0] : CATALOG_INVOKE
                )
        );

        return new DefaultCommand(CMD_INFO, infoQuery);
    }

    @Override
    protected List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "CATALOG",
                        Utils.join("<", ">", "|", catalogMap.keySet()),
                        true
                ).add(CATALOG_CLASS, "show transformed classes.")
                        .add(CATALOG_INVOKE, "show transformed classes, methods and constructors.", true)
                        .add(CATALOG_PROXY, "show transformed classes, methods, constructors and proxies.")
        );
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"info", "in"};
    }

    @Override
    public String getDesc() {
        return "Print information about transformed classes, \n" +
                "methods, constructors and proxies.";
    }
}
