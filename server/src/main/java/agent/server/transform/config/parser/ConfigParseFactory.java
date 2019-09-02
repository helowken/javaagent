package agent.server.transform.config.parser;

import agent.common.utils.Registry;
import agent.server.transform.config.ModuleConfig;

import java.util.List;

public class ConfigParseFactory {
    private static final Registry<ConfigParser.ConfigParserType, ConfigParser> registry = new Registry<>();

    static {
        reg(new FileConfigParser());
        reg(new RuleConfigParser());
    }

    private static void reg(ConfigParser configParser) {
        registry.reg(configParser.getType(), configParser);
    }

    public static List<ModuleConfig> parse(ConfigItem item) {
        return registry.get(item.getType()).parse(item);
    }
}
