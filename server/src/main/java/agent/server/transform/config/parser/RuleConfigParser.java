package agent.server.transform.config.parser;

import agent.base.utils.ReflectionUtils;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.parser.exception.ConfigParseException;
import agent.server.transform.config.parser.handler.AnnotationRuleConfigHandler;
import agent.server.transform.config.parser.handler.RuleConfigHandler;

import java.util.ArrayList;
import java.util.List;

public class RuleConfigParser implements ConfigParser {
    public static final RuleConfigParser instance = new RuleConfigParser();
    private static final List<RuleConfigHandler> handlerList = new ArrayList<>();
    private static final RuleConfigHandler defaultHandler = new AnnotationRuleConfigHandler();

    public static RuleConfigParser getInstance() {
        return instance;
    }

    private RuleConfigParser() {
    }

    public void reg(RuleConfigHandler handler) {
        if (!handlerList.contains(handler))
            handlerList.add(handler);
    }

    @Override
    public List<ModuleConfig> parse(ConfigItem item) throws ConfigParseException {
        RuleConfigItem ruleConfigItem = (RuleConfigItem) item;
        try {
            Class<?> clazz = findClass(ruleConfigItem.context, ruleConfigItem.className);
            Object instance = ReflectionUtils.newInstance(clazz);
            return handlerList.stream()
                    .filter(handler -> handler.accept(ruleConfigItem, instance))
                    .findFirst()
                    .orElse(defaultHandler)
                    .handle(ruleConfigItem, instance);
        } catch (Exception e) {
            throw new ConfigParseException("Config parse failed: " + item, e);
        }
    }

    private Class<?> findClass(String context, String className) {
        return TransformMgr.getInstance().getClassFinder().findClass(context, className);
    }

    @Override
    public ConfigParserType getType() {
        return ConfigParserType.RULE;
    }

    public static class RuleConfigItem implements ConfigItem {
        public final String context;
        public final String className;

        public RuleConfigItem(String context, String className) {
            this.context = context;
            this.className = className;
        }

        @Override
        public ConfigParserType getType() {
            return ConfigParserType.RULE;
        }
    }
}
