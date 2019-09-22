package agent.server.transform.config.parser.handler;

import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.parser.RuleConfigParser.RuleConfigItem;

import java.util.List;

public interface RuleConfigHandler {
    boolean accept(RuleConfigItem configItem, Object instance);

    List<ModuleConfig> handle(RuleConfigItem configItem, Object instance);
}
