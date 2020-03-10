package agent.client.command.result.handler;

import agent.base.utils.TypeObject;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.ClassResultEntity;
import agent.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static agent.base.utils.IndentUtils.*;

public class TestConfigResultHandler extends AbstractContextResultHandler {

    @Override
    public void handleSuccess(Command command, ExecResult result) {
        List<ClassResultEntity> rsList = JSONUtils.convert(
                result.getContent(),
                new TypeObject<List<ClassResultEntity>>() {
                }
        );
//        write("Test Config Result",
//                rsList,
//                (sb, configResultEntityList) ->
//                        configResultEntityList.forEach(configResultEntity ->
//                                configResultEntity.getClassEntityList().forEach(classResultEntity -> {
//                                            sb.append(INDENT_1).append("Class: ").append(classResultEntity.getClassName()).append("\n");
//                                            Map<String, List<String>> declareClassToMethods = new TreeMap<>();
//                                            classResultEntity.getInvokeList().forEach(methodResultEntity ->
//                                                    declareClassToMethods.computeIfAbsent(methodResultEntity.getDeclareClass(), key -> new ArrayList<>())
//                                                            .add(methodResultEntity.getName() + methodResultEntity.getDesc())
//                                            );
//                                            declareClassToMethods.forEach((declareClass, methods) -> {
//                                                sb.append(INDENT_2).append("From ").append(declareClass).append("\n");
//                                                methods.forEach(method -> {
//                                                    sb.append(INDENT_3).append("Method: ").append(method).append("\n");
//                                                });
//                                            });
//                                        }
//                                )
//                        )
//        );
    }
}
