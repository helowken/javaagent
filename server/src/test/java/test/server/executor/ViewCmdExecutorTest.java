package test.server.executor;

import agent.common.message.command.impl.ViewCommand;
import agent.server.command.executor.CmdExecutorMgr;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.invoke.MethodInvoke;
import org.junit.Test;
import test.server.AbstractTest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static agent.common.message.command.impl.ViewCommand.CATALOG_CLASS;
import static agent.common.message.command.impl.ViewCommand.CATALOG_INVOKE;

public class ViewCmdExecutorTest extends AbstractTest {
    @Test
    public void testContextToClass() {
        reg(A.class);
        reg(B.class);

        List classes = CmdExecutorMgr.exec(
                new ViewCommand(CATALOG_CLASS)
        ).getContent();
        System.out.println(classes);


        Map map = CmdExecutorMgr.exec(
                new ViewCommand(CATALOG_INVOKE)
        ).getContent();
        System.out.println(map);
    }

    private void reg(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            DestInvokeIdRegistry.getInstance().reg(
                    new MethodInvoke(method)
            );
        }
    }

    static class A {
        void a1() {
        }

        void a2(int a) {
        }
    }

    static class B {
        void b1() {
        }
    }
}
