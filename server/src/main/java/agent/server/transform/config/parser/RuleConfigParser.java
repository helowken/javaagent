package agent.server.transform.config.parser;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.*;
import agent.server.transform.config.parser.exception.ConfigParseException;
import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.ContextRule;
import agent.server.transform.config.rule.MethodRule;
import agent.server.transform.config.rule.MethodRule.Position;
import agent.server.transform.impl.dynamic.DynamicClassTransformer;
import agent.server.transform.impl.dynamic.DynamicConfigItem;
import agent.server.transform.impl.dynamic.MethodCallFilter;
import agent.server.transform.impl.dynamic.RuleValidateMgr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static agent.base.utils.Utils.blankToNull;
import static agent.base.utils.Utils.firstNotBlank;

public class RuleConfigParser implements ConfigParser {
    private static final Logger logger = Logger.getLogger(RuleConfigParser.class);

    @Override
    public List<ModuleConfig> parse(ConfigItem item) throws ConfigParseException {
        RuleConfigItem ruleConfigItem = (RuleConfigItem) item;
        try {
            Class<?> clazz = TransformMgr.getInstance().getClassFinder().findClass(ruleConfigItem.context, ruleConfigItem.className);
            Object instance = ReflectionUtils.newInstance(clazz);

            Map<String, ModuleConfig> contextToModuleConfig = new HashMap<>();
            Map<Method, MethodRule> methodToRule = filterRuleMethods(clazz.getDeclaredMethods());
            if (!methodToRule.isEmpty()) {
                String defaultContext = getContext(clazz);
                String defaultTargetClass = getTargetClass(clazz);
                methodToRule.forEach((method, methodRule) -> {
                    String context = getContext(method, defaultContext);
                    String targetClass = getTargetClass(method, defaultTargetClass);
                    String targetMethod = getTargetMethod(methodRule.method());
                    Position position = methodRule.position();
                    String mcFilterClass = methodRule.methodCallFilter();
                    logger.debug("Method: {}, context: {}, targetClass: {}, targetMethod: {}, position: {}, methodCallFilter: {}",
                            method, context, targetClass, targetMethod, position, mcFilterClass);

                    DynamicConfigItem configItem = new DynamicConfigItem(
                            position,
                            method,
                            instance,
                            newMethodCallFilter(Utils.blankToNull(mcFilterClass))
                    );
                    RuleValidateMgr.checkMethodValid(configItem);

                    MethodFilterConfig methodFilterConfig = new MethodFilterConfig();
                    methodFilterConfig.setIncludeExprSet(Collections.singleton(targetMethod));

                    ClassConfig classConfig = new ClassConfig();
                    classConfig.setTargetClass(targetClass);
                    classConfig.setMethodFilterConfig(methodFilterConfig);

                    TransformerConfig transformerConfig = new TransformerConfig();
                    transformerConfig.setRef(DynamicClassTransformer.REG_KEY);
                    transformerConfig.setConfig(Collections.singletonMap(DynamicClassTransformer.KEY_CONFIG, configItem));

                    TransformConfig transformConfig = new TransformConfig();
                    transformConfig.setTargetList(Collections.singletonList(classConfig));
                    transformConfig.setTransformerConfigList(Collections.singletonList(transformerConfig));

                    ModuleConfig moduleConfig = contextToModuleConfig.computeIfAbsent(context, key -> {
                        ModuleConfig mc = new ModuleConfig();
                        mc.setContextPath(context);
                        mc.setTransformConfigList(new ArrayList<>());
                        return mc;
                    });
                    moduleConfig.getTransformConfigList().add(transformConfig);
                });
            }
            logger.debug("context to moduleConfig: {}", contextToModuleConfig);
            return new ArrayList<>(contextToModuleConfig.values());
        } catch (Exception e) {
            throw new ConfigParseException("Config parse failed: " + item, e);
        }
    }

    private MethodCallFilter newMethodCallFilter(String mcFilterClassName) {
        try {
            return mcFilterClassName == null ? null : ReflectionUtils.newInstance(mcFilterClassName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTargetMethod(String methodExpr) {
        return Optional.ofNullable(
                blankToNull(methodExpr)
        ).orElseThrow(
                () -> new RuntimeException("Invalid ruleMethod: " + methodExpr)
        );
    }

    private String getContext(Class<?> clazz) {
        return getValue(
                () -> clazz.getAnnotation(ContextRule.class),
                ContextRule::value,
                null,
                null
        );
    }

    private String getContext(Method method, String defaultValue) {
        return getValue(
                () -> method.getAnnotation(ContextRule.class),
                ContextRule::value,
                defaultValue,
                "No context found for ruleMethod: " + method
        );
    }

    private String getTargetClass(Class<?> clazz) {
        return getValue(
                () -> clazz.getAnnotation(ClassRule.class),
                ClassRule::value,
                null,
                null
        );
    }

    private String getTargetClass(Method method, String defaultValue) {
        return getValue(
                () -> method.getAnnotation(ClassRule.class),
                ClassRule::value,
                defaultValue,
                "No target class found for ruleMethod: " + method
        );
    }

    private <T extends Annotation> String getValue(Supplier<T> supplier, Function<T, String> valueFunc, String defaultValue, String errMsg) {
        return firstNotBlank(
                errMsg,
                blankToNull(
                        Optional.ofNullable(supplier.get())
                                .map(valueFunc)
                                .orElse(null)
                ),
                defaultValue
        );
    }

    private Map<Method, MethodRule> filterRuleMethods(Method[] methods) {
        Map<Method, MethodRule> rsMap = new HashMap<>();
        if (methods != null) {
            for (Method method : methods) {
                MethodRule methodRule = method.getAnnotation(MethodRule.class);
                if (methodRule != null)
                    rsMap.put(method, methodRule);
            }
        }
        return rsMap;
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
