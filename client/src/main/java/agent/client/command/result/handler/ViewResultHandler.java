package agent.client.command.result.handler;

import agent.base.utils.IndentUtils;
import agent.client.utils.ClientLogger;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;
import agent.common.message.result.ExecResult;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ViewResultHandler extends AbstractContextResultHandler {

    @Override
    public void handleSuccess(Command command, ExecResult result) {
        Object content = result.getContent();
        StringBuilder sb = new StringBuilder();
        printContent(sb, 0, content);
        String msg = "Result of " + ((ViewCommand) command).getCatalog();
        ClientLogger.logger.info("{}: \n{}", msg, sb.toString());
    }

    private void printContent(StringBuilder sb, int level, Object content) {
        String indent = IndentUtils.getIndent(level);
        if (content instanceof Collection) {
            ((Collection) content).forEach(
                    el -> printContent(sb, level, el)
            );
        } else if (content instanceof Map) {
            ((Map) content).forEach(
                    (key, value) -> {
                        sb.append(indent).append(key).append(":\n");
                        printContent(sb, level + 1, value);
                    }
            );
        } else
            sb.append(indent).append(content).append("\n");
    }
}
