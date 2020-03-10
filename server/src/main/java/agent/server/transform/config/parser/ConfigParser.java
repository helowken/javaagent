package agent.server.transform.config.parser;

import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.parser.exception.ConfigParseException;

public interface ConfigParser {
    ModuleConfig parse(ConfigItem item) throws ConfigParseException;

    ConfigParserType getType();

    enum ConfigParserType {
        FILE
    }
}
