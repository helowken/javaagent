package agent.client.command.parser.impl;

import agent.base.help.HelpArg;
import agent.base.help.HelpInfo;
import agent.base.utils.TypeObject;
import agent.base.utils.Utils;
import agent.client.args.parse.CmdParams;
import agent.client.args.parse.InfoParamParser;
import agent.client.command.parser.exception.UnknownArgException;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.InfoQuery;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;
import agent.common.utils.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static agent.common.args.parse.FilterOptUtils.getUsageWithFilterRules;
import static agent.common.config.InfoQuery.*;
import static agent.common.message.MessageType.CMD_INFO;

public class InfoCmdParser extends AbstractCmdParser<CmdParams> {
    private static final String CATALOG_CLASS = "class";
    private static final String CATALOG_INVOKE = "invoke";
    private static final String CATALOG_PROXY = "proxy";
    private static final Map<String, Integer> catalogMap = new LinkedHashMap<>();
    private static HelpArg catalogArg;

    static {
        catalogMap.put(CATALOG_CLASS, INFO_CLASS);
        catalogMap.put(CATALOG_INVOKE, INFO_INVOKE);
        catalogMap.put(CATALOG_PROXY, INFO_PROXY);

        catalogArg = new HelpArg(
                "CATALOG",
                Utils.join("<", ">", "|", catalogMap.keySet())
        );
        catalogArg.add(CATALOG_CLASS, "show transformed classes.");
        catalogArg.add(CATALOG_INVOKE, "show transformed classes, methods and constructors.");
        catalogArg.add(CATALOG_PROXY, "show transformed classes, methods, constructors and proxies.", true);
    }

    @Override
    InfoParamParser createParamParser() {
        return new InfoParamParser();
    }

    @Override
    void checkParams(CmdParams params) {
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
                        args.length > 0 ? args[0] : CATALOG_PROXY
                )
        );

        return new MapCommand(
                CMD_INFO,
                JsonUtils.convert(
                        infoQuery,
                        new TypeObject<Map<String, Object>>() {
                        }
                )
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

    @Override
    HelpInfo getHelpUsage(CmdParams params) {
        return getUsageWithFilterRules(
                getCmdNames(),
                "[<CATALOG>]",
                catalogArg.getHelp()
        );
    }
}
