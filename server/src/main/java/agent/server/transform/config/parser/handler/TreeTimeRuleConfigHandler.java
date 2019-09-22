package agent.server.transform.config.parser.handler;

import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.parser.RuleConfigParser;

import java.util.List;

public class TreeTimeRuleConfigHandler implements RuleConfigHandler {
    @Override
    public boolean accept(RuleConfigParser.RuleConfigItem configItem, Object instance) {
        return false;
    }

    @Override
    public List<ModuleConfig> handle(RuleConfigParser.RuleConfigItem configItem, Object instance) {
        return null;
    }
}
