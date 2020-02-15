package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.server.transform.impl.ViewMgr;

import java.util.Map;

import static agent.common.message.MessageType.CMD_VIEW;
import static agent.common.message.command.impl.ViewCommand.*;
import static agent.server.transform.impl.ViewMgr.*;

@SuppressWarnings("unchecked")
class ViewCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        ViewCommand viewCmd = (ViewCommand) cmd;
        String catalog = viewCmd.getCatalog();
        Map<String, String> filterMap = viewCmd.getFilterMap();
        int maxLevel;
        switch (catalog) {
            case CATALOG_CONTEXT:
                maxLevel = VIEW_CONTEXT;
                break;
            case CATALOG_CLASS:
                maxLevel = VIEW_CLASS;
                break;
            case CATALOG_INVOKE:
                maxLevel = VIEW_INVOKE;
                break;
            case CATALOG_PROXY:
                maxLevel = VIEW_PROXY;
                break;
            default:
                throw new RuntimeException("Unknown catalog: " + catalog);
        }
        return DefaultExecResult.toSuccess(
                CMD_VIEW,
                null,
                ViewMgr.create(
                        maxLevel,
                        filterMap.get(CATALOG_CONTEXT),
                        filterMap.get(CATALOG_CLASS),
                        filterMap.get(CATALOG_INVOKE),
                        filterMap.get(CATALOG_PROXY)
                )
        );
    }

}
