package test.server.transform.executor;

import agent.common.message.command.CommandExecutor;
import agent.common.message.command.impl.ClasspathCommand;
import agent.server.command.executor.ClasspathCmdExecutor;
import agent.server.transform.TransformMgr;
import org.junit.Test;
import test.server.AbstractServerTest;

import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ClasspathCmdExecutorTest extends AbstractServerTest {
    @Test
    public void test() throws Exception {
        final String context = "test";
        final String url = "http://localhost:8080/";
        ClasspathCommand cmd = new ClasspathCommand(ClasspathCommand.ACTION_ADD, context, url);
        CommandExecutor cmdExecutor = new ClasspathCmdExecutor();
        cmdExecutor.exec(cmd);
        assertEquals(
                Collections.singletonMap(
                        context,
                        Collections.singleton(new URL(url))
                ),
                TransformMgr.getInstance().getContextToClasspathSet()
        );

        cmd = new ClasspathCommand(ClasspathCommand.ACTION_REMOVE, context, url);
        cmdExecutor.exec(cmd);
        assertEquals(
                Collections.emptyMap(),
                TransformMgr.getInstance().getContextToClasspathSet()
        );
    }
}
