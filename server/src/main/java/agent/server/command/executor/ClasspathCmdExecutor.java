package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ClasspathCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.ContextClassLoaderMgr;

import java.net.URL;

import static agent.common.message.command.impl.ClasspathCommand.*;

public class ClasspathCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        ClasspathCommand classPathCommand = (ClasspathCommand) cmd;
        String context = classPathCommand.getContext();
        URL url = parseURL(classPathCommand.getURL());
        final String action = classPathCommand.getAction();
        ContextClassLoaderMgr mgr = ContextClassLoaderMgr.getInstance();
        switch (action) {
            case ACTION_ADD:
                mgr.addClasspath(context, url);
                break;
            case ACTION_REMOVE:
                if (url == null)
                    mgr.clearClasspath(context);
                else
                    mgr.removeClasspath(context, url);
                break;
            case ACTION_REFRESH:
                if (url == null)
                    mgr.refreshClasspath(context);
                else
                    mgr.refreshClasspath(context, url);
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
