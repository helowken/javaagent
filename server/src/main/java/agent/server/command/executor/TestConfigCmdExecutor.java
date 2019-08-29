package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TestConfigByFileCommand;
import agent.common.message.command.impl.TestConfigByRuleCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.TestConfigResultEntity;
import agent.common.utils.JSONUtils;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.parser.ConfigParseFactory.ConfigItem;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static agent.common.message.MessageType.CMD_TEST_CONFIG_BY_FILE;
import static agent.common.message.MessageType.CMD_TEST_CONFIG_BY_RULE;
import static agent.common.message.result.entity.TestConfigResultEntity.ClassResultEntity;
import static agent.common.message.result.entity.TestConfigResultEntity.MethodResultEntity;
import static agent.server.transform.config.parser.ConfigParser.ConfigParserType.FILE;
import static agent.server.transform.config.parser.ConfigParser.ConfigParserType.RULE;

class TestConfigCmdExecutor extends AbstractCmdExecutor {
    private static final String SELF = "self";

    @Override
    ExecResult doExec(Command cmd) {
        int cmdType = cmd.getType();
        ConfigItem item;
        switch (cmdType) {
            case CMD_TEST_CONFIG_BY_FILE:
                item = new ConfigItem(FILE,
                        ((TestConfigByFileCommand) cmd).getConfig()
                );
                break;
            case CMD_TEST_CONFIG_BY_RULE:
                item = new ConfigItem(RULE,
                        ((TestConfigByRuleCommand) cmd).getConfig()
                );
                break;
            default:
                throw new RuntimeException("Invalid cmd type: " + cmdType);
        }
        return DefaultExecResult.toSuccess(cmd.getType(),
                "Test config successfully.",
                JSONUtils.convert(
                        testConfig(item),
                        new TypeReference<Map<String, List<Map>>>() {
                        }
                )
        );
    }

    private Map<String, List<TestConfigResultEntity>> testConfig(ConfigItem item) {
        Map<String, List<TestConfigResultEntity>> contextToConfigResultEntityList = new HashMap<>();
        TransformMgr.getInstance().searchMethods(item, (context, result) -> {
            if (!result.methodList.isEmpty()) {
                List<TestConfigResultEntity> configResultEntityList = contextToConfigResultEntityList.computeIfAbsent(context, key -> new ArrayList<>());

                TestConfigResultEntity configResultEntity = new TestConfigResultEntity();
                configResultEntity.setContext(context);
                configResultEntityList.add(configResultEntity);

                ClassResultEntity classResultEntity = new ClassResultEntity();
                String className = result.ctClass.getName();
                classResultEntity.setClassName(className);
                configResultEntity.addClassEntity(classResultEntity);

                result.methodList.forEach(method -> {
                    String declareClass = method.getDeclaringClass().getName();
                    if (declareClass.equals(className))
                        declareClass = SELF;
                    MethodResultEntity methodResultEntity = new MethodResultEntity();
                    methodResultEntity.setDeclareClass(declareClass);
                    methodResultEntity.setMethodName(method.getName());
                    methodResultEntity.setSignature(method.getSignature());
                    classResultEntity.addMethodEntity(methodResultEntity);
                });
            }
        });
        return contextToConfigResultEntityList;
    }
}
