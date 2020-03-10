package agent.server.command.executor;

import agent.base.utils.TypeObject;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand.TestConfigByFileCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.ClassResultEntity;
import agent.common.utils.JSONUtils;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.FileConfigParser;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.*;

import static agent.common.message.MessageType.CMD_TEST_CONFIG_BY_FILE;

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
            default:
                throw new RuntimeException("Invalid cmd type: " + cmdType);
        }
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                "Test config successfully.",
                JSONUtils.convert(
                        testConfig(item),
                        new TypeObject<List<Map>>() {
                        }
                )
        );
    }

    private List<ClassResultEntity> testConfig(ConfigItem item) {
        Collection<DestInvoke> invokes = TransformMgr.getInstance().searchInvokes(item);
        List<ClassResultEntity> rsList = new ArrayList<>();
        if (!invokes.isEmpty()) {
            Map<Class<?>, ClassResultEntity> classToEntity = new HashMap<>();
            invokes.forEach(invoke -> classToEntity.computeIfAbsent(
                    invoke.getDeclaringClass(),
                    clazz -> {
                        ClassResultEntity classResultEntity = new ClassResultEntity();
                        classResultEntity.setClassName(
                                clazz.getName()
                        );
                        rsList.add(classResultEntity);
                        return classResultEntity;
                    }
                    ).addInvoke(
                    invoke.getFullName()
                    )
            );
        }
        return rsList;
    }
}
