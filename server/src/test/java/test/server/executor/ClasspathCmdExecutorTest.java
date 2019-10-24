package test.server.executor;

import agent.common.message.command.CommandExecutor;
import agent.common.message.command.impl.ClasspathCommand;
import agent.server.command.executor.ClasspathCmdExecutor;
import agent.server.transform.ContextClassLoaderMgr;
import org.junit.Test;
import test.server.AbstractServerTest;

import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ClasspathCmdExecutorTest extends AbstractServerTest {
    private static final ContextClassLoaderMgr mgr = ContextClassLoaderMgr.getInstance();

    @Test
    public void test() throws Exception {
        final String context = "test";
        final String url = "http://localhost:8080/";
        classFinder.setContextLoader(context);
        ClasspathCommand cmd = new ClasspathCommand(ClasspathCommand.ACTION_ADD, context, url);
        CommandExecutor cmdExecutor = new ClasspathCmdExecutor();
        cmdExecutor.exec(cmd);
        assertEquals(
                Collections.singletonMap(
                        context,
                        Collections.singleton(new URL(url))
                ),
                mgr.getContextToClasspathSet()
        );

        cmd = new ClasspathCommand(ClasspathCommand.ACTION_REMOVE, context, url);
        cmdExecutor.exec(cmd);
        assertEquals(
                Collections.emptyMap(),
                mgr.getContextToClasspathSet()
        );
    }
}
