package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.InfoCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.server.transform.impl.InfoMgr;

import java.util.Map;

import static agent.common.message.MessageType.CMD_VIEW;
import static agent.common.message.command.impl.InfoCommand.*;
import static agent.server.transform.impl.InfoMgr.*;

@SuppressWarnings("unchecked")
class InfoCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        InfoCommand viewCmd = (InfoCommand) cmd;
        String catalog = viewCmd.getCatalog();
        Map<String, String> filterMap = viewCmd.getFilterMap();
        int maxLevel;
        switch (catalog) {
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
                InfoMgr.create(
                        maxLevel,
                        filterMap.get(CATALOG_CLASS),
                        filterMap.get(CATALOG_INVOKE),
                        filterMap.get(CATALOG_PROXY)
                )
        );
    }

}
