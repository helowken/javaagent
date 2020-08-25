package test.server.executor;

import agent.common.message.command.impl.InfoCommand;
import agent.invoke.MethodInvoke;
import agent.server.command.executor.CmdExecutorMgr;
import agent.server.transform.impl.DestInvokeIdRegistry;
import org.junit.Test;
import test.server.AbstractTest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static agent.common.message.command.impl.InfoCommand.CATALOG_CLASS;
import static agent.common.message.command.impl.InfoCommand.CATALOG_INVOKE;

public class InfoCmdExecutorTest extends AbstractTest {
    @Test
    public void testContextToClass() {
        reg(A.class);
        reg(B.class);

        List classes = CmdExecutorMgr.exec(
                new InfoCommand(CATALOG_CLASS)
        ).getContent();
        System.out.println(classes);


        Map map = CmdExecutorMgr.exec(
                new InfoCommand(CATALOG_INVOKE)
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
