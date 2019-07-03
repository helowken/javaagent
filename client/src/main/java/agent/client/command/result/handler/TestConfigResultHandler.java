package agent.client.command.result.handler;

import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.base.utils.IndentUtils;

import java.util.List;
import java.util.Map;

public class TestConfigResultHandler extends AbstractContextResultHandler {

    @Override
    public void handleSuccess(Command command, ExecResult result) {
        Map<String, Map<String, Map<String, List<String>>>> rsMap = result.getContent();
        write("Test Config Result", rsMap, (sb, classToMethods) ->
                classToMethods.forEach((className, declareClassToMethods) -> {
                    sb.append(IndentUtils.getIndent(1)).append("class: ").append(className).append("\n");
                    declareClassToMethods.forEach((declareClass, methods) -> {
                        sb.append(IndentUtils.getIndent(2)).append("from ").append(declareClass).append("\n");
                        methods.forEach(method -> {
                            sb.append(IndentUtils.getIndent(3)).append("method: ").append(method).append("\n");
                        });
                    });
                })
        );
    }
}
