package agent.server.transform.config.parser.handler;

import agent.server.transform.TransformMgr;
import agent.server.transform.config.*;
import agent.server.transform.config.rule.MethodRule;
import agent.server.transform.impl.dynamic.DynamicClassTransformer;
import agent.server.transform.impl.dynamic.DynamicConfigItem;
import agent.server.transform.impl.dynamic.RuleValidateMgr;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractRuleConfigHandler implements RuleConfigHandler {
    Class<?> findClass(String context, String className) {
        return TransformMgr.getInstance().getClassFinder().findClass(context, className);
    }

    TransformConfig newTransformConfig(String targetMethod, String targetClass, DynamicConfigItem configItem) {
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

        return transformConfig;
    }

    ModuleConfig newModuleConfig(String context) {
        ModuleConfig mc = new ModuleConfig();
        mc.setContextPath(context);
        mc.setTransformConfigList(new ArrayList<>());
        return mc;
    }

    Map<Method, MethodRule> filterRuleMethods(Method[] methods) {
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
}