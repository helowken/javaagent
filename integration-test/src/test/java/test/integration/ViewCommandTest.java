package test.integration;

import agent.client.command.result.CommandResultHandlerMgr;
import agent.common.message.command.impl.ViewCommand;
import agent.common.message.result.ExecResult;
import agent.server.command.executor.CmdExecutorMgr;
import org.junit.Test;
import test.server.transform.AbstractViewTest;

import static agent.common.message.command.impl.ViewCommand.*;

public class ViewCommandTest extends AbstractViewTest {

    @Test
    public void test() {
        String[] catalogs = {
                CATALOG_CONTEXT,
                CATALOG_CLASS,
                CATALOG_INVOKE,
                CATALOG_PROXY
        };
        for (String catalog : catalogs) {
            ViewCommand cmd = new ViewCommand(catalog);
            ExecResult result = CmdExecutorMgr.exec(cmd);
            CommandResultHandlerMgr.handleResult(cmd, result);
            System.out.println("==================");
        }
    }


}
