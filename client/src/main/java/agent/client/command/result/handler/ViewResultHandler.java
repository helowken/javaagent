package agent.client.command.result.handler;

import agent.base.utils.IndentUtils;
import agent.client.utils.ClientLogger;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings("unchecked")
class ViewResultHandler extends AbstractExecResultHandler {

    @Override
    public void handleSuccess(Command command, ExecResult result) {
        StringBuilder sb = new StringBuilder();
        fillContent(result, sb);
        ClientLogger.logger.info(
                "Result: \n{}",
                sb.toString()
        );
    }

    void fillContent(ExecResult result, StringBuilder sb) {
        Object content = result.getContent();
        if (content != null)
            printContent(sb, 0, content);
        else
            sb.append("No content.");
    }

    private void printContent(StringBuilder sb, int level, Object content) {
        String indent = IndentUtils.getIndent(level);
        if (content instanceof Collection)
            ((Collection) content).forEach(
                    el -> printContent(sb, level, el)
            );
        else if (content instanceof Map)
            ((Map) content).forEach(
                    (key, value) -> {
                        sb.append(indent).append(key).append(":\n");
                        printContent(sb, level + 1, value);
                    }
            );
        else if (content != null)
            sb.append(indent).append(content).append("\n");
    }
}
