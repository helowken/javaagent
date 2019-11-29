package agent.server.transform.config.parser.handler;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.parser.RuleConfigParser.RuleConfigItem;
import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.MethodRule;
import agent.server.transform.config.rule.MethodRule.Position;
import agent.server.transform.impl.dynamic.DynamicConfigItem;
import agent.server.transform.impl.dynamic.MethodRuleFilter;

import java.lang.reflect.Method;
import java.util.*;

import static agent.base.utils.Utils.blankToNull;

public class AnnotationRuleConfigHandler extends AbstractRuleConfigHandler {
    private static final Logger logger = Logger.getLogger(AnnotationRuleConfigHandler.class);

    @Override
    public boolean accept(RuleConfigItem item, Object instance) {
        return true;
    }

    @Override
    public List<ModuleConfig> handle(RuleConfigItem item, Object instance) {
        Class<?> clazz = instance.getClass();
        Map<String, ModuleConfig> contextToModuleConfig = new HashMap<>();
        Map<Method, MethodRule> methodToRule = filterRuleMethods(clazz.getDeclaredMethods());
        final String context = item.context;
        final String targetClass = getTargetClass(clazz);
        if (!methodToRule.isEmpty()) {
            methodToRule.forEach((method, methodRule) -> {
                String targetMethod = getTargetMethod(methodRule.method());
                Position position = methodRule.position();
                String mcFilterClass = methodRule.filter();
                int maxLevel = methodRule.maxLevel();
                logger.debug("Method: {}, context: {}, targetClass: {}, targetMethod: {}, position: {}, filter: {}, maxLevel: {}",
                        method, context, targetClass, targetMethod, position, mcFilterClass, maxLevel);

                DynamicConfigItem configItem = new DynamicConfigItem(
                        context,
                        position,
                        method,
                        instance,
                        newMethodRuleFilter(context, Utils.blankToNull(mcFilterClass)),
                        maxLevel
                );

                contextToModuleConfig.computeIfAbsent(context, this::newModuleConfig)
                        .getTransformConfigs()
                        .add(
                                newTransformConfig(targetMethod, targetClass, configItem)
                        );
            });
        }
        logger.debug("context to moduleConfig: {}", contextToModuleConfig);
        return new ArrayList<>(contextToModuleConfig.values());
    }

    private MethodRuleFilter newMethodRuleFilter(String context, String mcFilterClassName) {
        try {
            return mcFilterClassName == null ?
                    null :
                    ReflectionUtils.newInstance(
                            findClass(context, mcFilterClassName)
                    );
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

    private String getTargetClass(Class<?> clazz) {
        String targetClass = null;
        ClassRule classRule = clazz.getAnnotation(ClassRule.class);
        if (classRule != null)
            targetClass = blankToNull(
                    classRule.value()
            );
        if (targetClass == null)
            throw new RuntimeException("Invalid target class.");
        return targetClass;
    }
}
