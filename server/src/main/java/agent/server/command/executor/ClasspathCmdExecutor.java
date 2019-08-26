package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ClasspathCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;

import java.net.URL;

import static agent.common.message.command.impl.ClasspathCommand.*;

public class ClasspathCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        ClasspathCommand classPathCommand = (ClasspathCommand) cmd;
        String context = classPathCommand.getContext();
        URL url = new URL(classPathCommand.getURL());
        TransformMgr transformMgr = TransformMgr.getInstance();
        switch (classPathCommand.getAction()) {
            case ACTION_ADD:
                transformMgr.addURL(context, url);
                break;
            case ACTION_REFRESH:
                transformMgr.refreshURL(context, url);
                break;
            case ACTION_REMOVE:
                transformMgr.removeURL(context, url);
                break;
        }
        return null;
    }
}
