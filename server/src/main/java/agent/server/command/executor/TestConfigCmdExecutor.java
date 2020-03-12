package agent.server.command.executor;

import agent.base.utils.TypeObject;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand.TestConfigByFileCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.ClassResultEntity;
import agent.common.utils.JSONUtils;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.parser.ConfigParser;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.*;

public class TestConfigCmdExecutor extends AbstractCmdExecutor {

    @Override
    ExecResult doExec(Command cmd) {
        ModuleConfig moduleConfig = ConfigParser.parse(
                ((TestConfigByFileCommand) cmd).getConfig()
        );
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                "Test config successfully.",
                JSONUtils.convert(
                        testConfig(moduleConfig),
                        new TypeObject<List<Map>>() {
                        }
                )
        );
    }

    private List<ClassResultEntity> testConfig(ModuleConfig moduleConfig) {
        Collection<DestInvoke> invokes = TransformMgr.getInstance().searchInvokes(moduleConfig);
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
