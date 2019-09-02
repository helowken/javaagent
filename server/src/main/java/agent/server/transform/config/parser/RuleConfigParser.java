package agent.server.transform.config.parser;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.*;
import agent.server.transform.config.parser.exception.ConfigParseException;
import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.ContextRule;
import agent.server.transform.config.rule.MethodRule;
import agent.server.transform.impl.DynamicClassTransformer;

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

            List<ModuleConfig> moduleConfigList = new ArrayList<>();
            Map<Method, MethodRule> methodToRule = filterRuleMethods(clazz.getDeclaredMethods());
            if (!methodToRule.isEmpty()) {
                String defaultContext = getContext(clazz);
                String defaultTargetClass = getTargetClass(clazz);
                methodToRule.forEach((method, methodRule) -> {
                    String context = getContext(method, defaultContext);
                    String targetClass = getTargetClass(method, defaultTargetClass);
                    String targetMethod = Optional.ofNullable(
                            blankToNull(
                                    methodRule.method()
                            )
                    ).orElseThrow(
                            () -> new RuntimeException("Invalid method: " + methodRule.method())
                    );
                    MethodRule.Position position = methodRule.position();
                    logger.debug("Method: {}, context: {}, targetClass: {}, targetMethod: {}, position: {}",
                            method, context, targetClass, targetMethod, position);

                    MethodFilterConfig methodFilterConfig = new MethodFilterConfig();
                    methodFilterConfig.setIncludeExprSet(Collections.singleton(targetMethod));

                    ClassConfig classConfig = new ClassConfig();
                    classConfig.setTargetClass(targetClass);
                    classConfig.setMethodFilterConfig(methodFilterConfig);
                    List<ClassConfig> classConfigList = new ArrayList<>();
                    classConfigList.add(classConfig);

                    Map<String, Object> config = new HashMap<>();
                    config.put(DynamicClassTransformer.KEY_POSITION, position);
                    config.put(DynamicClassTransformer.KEY_METHOD, method);
                    config.put(DynamicClassTransformer.KEY_INSTANCE, instance);

                    TransformerConfig transformerConfig = new TransformerConfig();
                    transformerConfig.setRef(DynamicClassTransformer.REG_KEY);
                    transformerConfig.setConfig(config);
                    List<TransformerConfig> transformerConfigList = new ArrayList<>();
                    transformerConfigList.add(transformerConfig);

                    TransformConfig transformConfig = new TransformConfig();
                    transformConfig.setTargetList(classConfigList);
                    transformConfig.setTransformerConfigList(transformerConfigList);

                    ModuleConfig moduleConfig = new ModuleConfig();
                    moduleConfig.setContextPath(context);
                    moduleConfig.setTransformConfigList(Collections.singletonList(transformConfig));
                    logger.debug("moduleConfig: {}", moduleConfig);

                    moduleConfigList.add(moduleConfig);
                });
            }
            return moduleConfigList;
        } catch (Exception e) {
            throw new ConfigParseException("Config parse failed: " + item, e);
        }
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
                "No context found for method: " + method
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
                "No target class found for method: " + method
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
