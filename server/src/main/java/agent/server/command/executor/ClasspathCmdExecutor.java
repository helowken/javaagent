package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ClasspathCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;

import java.net.URL;

import static agent.common.message.command.impl.ClasspathCommand.ACTION_ADD;
import static agent.common.message.command.impl.ClasspathCommand.ACTION_REMOVE;

public class ClasspathCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        ClasspathCommand classPathCommand = (ClasspathCommand) cmd;
        String context = classPathCommand.getContext();
        URL url = parseURL(classPathCommand.getURL());
        TransformMgr transformMgr = TransformMgr.getInstance();
        final String action = classPathCommand.getAction();
        switch (action) {
            case ACTION_ADD:
                transformMgr.addClasspath(context, url);
                break;
            case ACTION_REMOVE:
                if (url == null)
                    transformMgr.clearClasspath(context);
                else
                    transformMgr.removeClasspath(context, url);
                break;
            default:
                throw new RuntimeException("Unknown action: " + action);
        }
        return null;
    }

    private URL parseURL(String s) {
        if (s == null)
            return null;
        try {
            return new URL(s);
        } catch (Exception e) {
            if (s.startsWith("/")) {
                String t = "file://" + s;
                if (!s.endsWith("/"))
                    t += "/";
                return parseURL(t);
            }
            throw new RuntimeException("Invalid url: " + s);
        }
    }
}
