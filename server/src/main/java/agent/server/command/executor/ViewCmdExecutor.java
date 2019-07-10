package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static agent.common.message.MessageType.CMD_VIEW;
import static agent.common.message.command.impl.ViewCommand.CATALOG_CLASS;

public class ViewCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        String catalog = ((ViewCommand) cmd).getCatalog();
        Object value;
        switch (catalog) {
            case CATALOG_CLASS:
                value = getContextToClassSet();
                break;
            default:
                throw new RuntimeException("Unknown catalog: " + catalog);
        }
        if (value == null)
            throw new RuntimeException("No result found.");
        return DefaultExecResult.toSuccess(CMD_VIEW, null, value);
    }

    private Map<String, Set<String>> getContextToClassSet() {
        Map<String, Set<String>> rsMap = new HashMap<>();
        TransformMgr.getInstance()
                .getContextToTransformedClassSet()
                .forEach((context, classSet) ->
                        rsMap.put(context,
                                classSet.stream()
                                        .map(Class::getName)
                                        .collect(Collectors.toSet())
                        )
                );
        return rsMap;
    }
}
