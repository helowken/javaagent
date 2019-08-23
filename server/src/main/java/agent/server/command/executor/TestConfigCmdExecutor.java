package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TestConfigCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.TestConfigResultEntity;
import agent.common.utils.JSONUtils;
import agent.server.transform.TransformMgr;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static agent.common.message.result.entity.TestConfigResultEntity.ClassResultEntity;
import static agent.common.message.result.entity.TestConfigResultEntity.MethodResultEntity;

class TestConfigCmdExecutor extends AbstractCmdExecutor {
    private static final String SELF = "self";

    @Override
    ExecResult doExec(Command cmd) throws Exception {
        byte[] config = ((TestConfigCommand) cmd).getConfig();
        Map<String, List<TestConfigResultEntity>> contextToConfigResultEntityList = new HashMap<>();
        TransformMgr.getInstance().searchMethods(config, (context, result) -> {
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
        return DefaultExecResult.toSuccess(cmd.getType(),
                "Test config successfully.",
                JSONUtils.convert(contextToConfigResultEntityList, new TypeReference<Map<String, List<Map>>>() {
                })
        );
    }
}
