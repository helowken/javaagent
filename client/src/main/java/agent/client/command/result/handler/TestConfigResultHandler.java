package agent.client.command.result.handler;

import agent.base.utils.IndentUtils;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.TestConfigResultEntity;
import agent.common.utils.JSONUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TestConfigResultHandler extends AbstractContextResultHandler {

    @Override
    public void handleSuccess(Command command, ExecResult result) {
        Map<String, List<TestConfigResultEntity>> contextToConfigResultEntityList = JSONUtils.convert(result.getContent(),
                new TypeReference<Map<String, List<TestConfigResultEntity>>>() {
                }
        );
        write("Test Config Result",
                contextToConfigResultEntityList,
                (sb, configResultEntityList) ->
                        configResultEntityList.forEach(configResultEntity ->
                                configResultEntity.getClassEntityList().forEach(classResultEntity -> {
                                            sb.append(IndentUtils.getIndent(1)).append("Class: ").append(classResultEntity.getClassName()).append("\n");
                                            Map<String, List<String>> declareClassToMethods = new TreeMap<>();
                                            classResultEntity.getMethodEntityList().forEach(methodResultEntity ->
                                                    declareClassToMethods.computeIfAbsent(methodResultEntity.getDeclareClass(), key -> new ArrayList<>())
                                                            .add(methodResultEntity.getMethodName() + methodResultEntity.getSignature())
                                            );
                                            declareClassToMethods.forEach((declareClass, methods) -> {
                                                sb.append(IndentUtils.getIndent(2)).append("From ").append(declareClass).append("\n");
                                                methods.forEach(method -> {
                                                    sb.append(IndentUtils.getIndent(3)).append("Method: ").append(method).append("\n");
                                                });
                                            });
                                        }
                                )
                        )
        );
    }
}
