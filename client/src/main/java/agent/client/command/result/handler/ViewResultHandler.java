package agent.client.command.result.handler;

import agent.base.utils.IndentUtils;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;
import agent.common.message.result.ExecResult;

import java.util.Map;
import java.util.Set;

import static agent.common.message.command.impl.ViewCommand.CATALOG_CLASS;
import static agent.common.message.command.impl.ViewCommand.CATALOG_CLASSPATH;

public class ViewResultHandler extends AbstractContextResultHandler {

    @Override
    public void handleSuccess(Command command, ExecResult result) {
        Map<String, Set<String>> rsMap = result.getContent();
        ViewCommand viewCommand = (ViewCommand) command;
        String title;
        String entryDesc;
        final String catalog = viewCommand.getCatalog();
        switch (catalog) {
            case CATALOG_CLASS:
                title = "View Transformed Class Result";
                entryDesc = "class: ";
                break;
            case CATALOG_CLASSPATH:
                title = "View Classpath Result";
                entryDesc = "classpath: ";
                break;
            default:
                throw new RuntimeException("Unknown catalog: " + catalog);
        }
        write(title, rsMap, (sb, classSet) ->
                classSet.forEach(className ->
                        sb.append(IndentUtils.getIndent(1)).append(entryDesc).append(className).append("\n")
                )
        );
    }
}
