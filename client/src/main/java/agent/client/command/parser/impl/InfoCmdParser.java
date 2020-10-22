package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.help.HelpArg;
import agent.base.utils.Utils;
import agent.client.args.parse.DefaultParamParser;
import agent.client.command.parser.exception.UnknownArgException;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.InfoQuery;
import agent.common.message.command.Command;
import agent.common.message.command.impl.PojoCommand;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.config.InfoQuery.*;
import static agent.common.message.MessageType.CMD_INFO;

public class InfoCmdParser extends AbstractCmdParser<CmdParams> {
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
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                getFilterOptParsers()
        );
    }

    @Override
    void checkParams(CmdParams params) {
        super.checkParams(params);
        String[] args = params.getArgs();
        if (args.length > 0 && !catalogMap.containsKey(args[0]))
            throw new UnknownArgException(args[0]);
    }

    @Override
    Command createCommand(CmdParams params) {
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

        return new PojoCommand(CMD_INFO, infoQuery);
    }

    @Override
    List<HelpArg> createHelpArgList() {
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
