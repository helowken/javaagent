package agent.client.command.result.handler;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.IndentUtils;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings("unchecked")
public class InfoResultHandler extends AbstractExecResultHandler {

    @Override
    public void handleSuccess(Command command, ExecResult result) {
        super.handleSuccess(command, result);
        StringBuilder sb = new StringBuilder();
        fillContent(result, sb);
        ConsoleLogger.getInstance().info("{}", sb.toString());
    }

    private void fillContent(ExecResult result, StringBuilder sb) {
        Object content = result.getContent();
        if (content != null)
            printContent(sb, 0, content);
        else
            sb.append("\nNone.");
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
                        if (level == 0)
                            sb.append('\n');
                        sb.append(indent).append(key).append(":\n");
                        printContent(sb, level + 1, value);
                    }
            );
        else if (content != null) {
            String s = content.toString();
            String[] ts = s.split("\n");
            for (String t : ts) {
                sb.append(indent).append(t).append('\n');
            }
        }
    }

}
