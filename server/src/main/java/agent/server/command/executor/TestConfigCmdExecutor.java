package agent.server.command.executor;

import agent.base.utils.TypeObject;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand.TestConfigByFileCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.TestConfigResultEntity;
import agent.common.utils.JSONUtils;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.FileConfigParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static agent.common.message.MessageType.CMD_TEST_CONFIG_BY_FILE;
import static agent.common.message.result.entity.TestConfigResultEntity.ClassResultEntity;
import static agent.common.message.result.entity.TestConfigResultEntity.InvokeResultEntity;

public class TestConfigCmdExecutor extends AbstractCmdExecutor {

    @Override
    ExecResult doExec(Command cmd) {
        int cmdType = cmd.getType();
        ConfigItem item;
        switch (cmdType) {
            case CMD_TEST_CONFIG_BY_FILE:
                item = new FileConfigParser.FileConfigItem(
                        ((TestConfigByFileCommand) cmd).getConfig()
                );
                break;
//            case CMD_TEST_CONFIG_BY_RULE:
//                TestConfigByRuleCommand ruleCmd = (TestConfigByRuleCommand) cmd;
//                item = new RuleConfigParser.RuleConfigItem(
//                        ruleCmd.getContext(),
//                        ruleCmd.getClassName()
//                );
//                break;
            default:
                throw new RuntimeException("Invalid cmd type: " + cmdType);
        }
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                "Test config successfully.",
                JSONUtils.convert(
                        testConfig(item),
                        new TypeObject<Map<String, List<Map>>>() {
                        }
                )
        );
    }

    private Map<String, List<TestConfigResultEntity>> testConfig(ConfigItem item) {
        Map<String, List<TestConfigResultEntity>> contextToConfigResultEntityList = new HashMap<>();
        TransformMgr.getInstance().searchInvokes(item, (context, result) -> {
            if (!result.invokes.isEmpty()) {
                List<TestConfigResultEntity> configResultEntityList = contextToConfigResultEntityList.computeIfAbsent(context, key -> new ArrayList<>());

                TestConfigResultEntity configResultEntity = new TestConfigResultEntity();
                configResultEntity.setContext(context);
                configResultEntityList.add(configResultEntity);

                ClassResultEntity classResultEntity = new ClassResultEntity();
                String className = result.clazz.getName();
                classResultEntity.setClassName(className);
                configResultEntity.addClassEntity(classResultEntity);

                result.invokes.forEach(invoke -> {
                    InvokeResultEntity invokeResultEntity = new InvokeResultEntity();
                    invokeResultEntity.setDeclareClass(result.clazz.getName());
                    invokeResultEntity.setName(invoke.getName());
                    invokeResultEntity.setDesc(invoke.getDescriptor());
                    classResultEntity.addInvokeEntity(invokeResultEntity);
                });
            }
        });
        return contextToConfigResultEntityList;
    }
}
