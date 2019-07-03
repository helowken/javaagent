package agent.client.command.result.handler;

import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.base.utils.IndentUtils;

import java.util.Map;
import java.util.Set;

public class ViewResultHandler extends AbstractContextResultHandler {

    @Override
    public void handleSuccess(Command command, ExecResult result) {
        Map<String, Set<String>> rsMap = result.getContent();
        write("View Class Result", rsMap, (sb, classSet) ->
                classSet.forEach(className ->
                        sb.append(IndentUtils.getIndent(1)).append("class: ").append(className).append("\n")
                )
        );
    }
}
