package agent.server.listener;

import agent.base.utils.Utils;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.cmdline.command.result.ExecResult;
import agent.server.ServerListener;
import agent.server.command.client.ClientUtils;
import agent.server.command.executor.ServerCmdExecMgr;

import java.io.File;
import java.util.List;

public class StartupScriptListener implements ServerListener {
    @Override
    public void onStartup(Object[] args) {
        if (args.length < 2)
            return;
        String scriptPath = (String) args[1];
        if (Utils.isNotBlank(scriptPath)) {
            File scriptFile = new File(scriptPath);
            if (!scriptFile.exists())
                throw new RuntimeException("File not found: " + scriptFile.getAbsolutePath());

            List<CmdItem> cmdItemList = ClientUtils.parseCommand(
                    "file",
                    new String[]{
                            scriptFile.getAbsolutePath()
                    }
            );
            Command cmd;
            for (CmdItem item : cmdItemList) {
                item.print();
                cmd = item.getCmd();
                if (cmd != null) {
                    ExecResult rs = ServerCmdExecMgr.exec(cmd);
                    ClientUtils.handleResult(cmd, rs);
                    if (!rs.isSuccess())
                        break;
                }
            }
        }
    }

    @Override
    public void onShutdown() {
    }
}
