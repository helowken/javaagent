package agent.server.transform.config.parser.handler;

import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.parser.RuleConfigParser;
import agent.server.transform.config.rule.MethodRule.Position;
import agent.server.transform.impl.dynamic.DynamicConfigItem;
import agent.server.transform.impl.dynamic.rule.TraverseRule;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class TreeRuleConfigHandler extends AbstractRuleConfigHandler {

    @Override
    public boolean accept(RuleConfigParser.RuleConfigItem configItem, Object instance) {
        return instance instanceof TreeRule;
    }

    @Override
    public List<ModuleConfig> handle(RuleConfigParser.RuleConfigItem ruleConfigItem, Object instance) {
        TreeRule treeRule = (TreeRule) instance;
        ModuleConfig moduleConfig = newModuleConfig(
                treeRule.getContext()
        );
        filterRuleMethods(
                TraverseRule.class.getDeclaredMethods()
        ).forEach(
                (method, methodRule) -> addTransformConfig(
                        moduleConfig,
                        treeRule,
                        method,
                        methodRule.position()
                )
        );
        return Collections.singletonList(moduleConfig);
    }

    private void addTransformConfig(ModuleConfig moduleConfig, TreeRule treeRule, Method method, Position position) {
        TraverseRule traverseRule = treeRule.getTraverseRule();
        DynamicConfigItem configItem = new DynamicConfigItem(
                treeRule.getContext(),
                position,
                method,
                traverseRule,
                treeRule,
                treeRule.getMaxLevel()
        );
        moduleConfig.getTransformConfigList()
                .add(
                        newTransformConfig(
                                treeRule.getTargetMethod(),
                                treeRule.getTargetClass(),
                                configItem
                        )
                );
    }

}
