package agent.server.transform.config.parser;

import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.parser.exception.ConfigParseException;

import java.util.List;

public interface ConfigParser {
    List<ModuleConfig> parse(Object source) throws ConfigParseException;

    ConfigParserType getType();

    enum ConfigParserType {
        FILE, RULE
    }
}
