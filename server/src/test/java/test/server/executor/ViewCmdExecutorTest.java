package test.server.executor;

import agent.common.message.command.impl.ViewCommand;
import agent.server.command.executor.CmdExecutorMgr;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.invoke.MethodInvoke;
import org.junit.Test;
import test.server.AbstractTest;

import java.lang.reflect.Method;
import java.util.Map;

import static agent.common.message.command.impl.ViewCommand.CATALOG_CLASS;
import static agent.common.message.command.impl.ViewCommand.CATALOG_INVOKE;

public class ViewCmdExecutorTest extends AbstractTest {
    private static final String context1 = "context1";
    private static final String context2 = "context2";

    @Test
    public void testContextToClass() throws Exception {
        reg(context1, A.class);
        reg(context2, B.class);

        Map map = CmdExecutorMgr.exec(
                new ViewCommand(CATALOG_CLASS)
        ).getContent();
        System.out.println(map);


        map = CmdExecutorMgr.exec(
                new ViewCommand(CATALOG_INVOKE)
        ).getContent();
        System.out.println(map);
    }

    private void reg(String context, Class<?> clazz, String methodName) throws Exception {
        DestInvokeIdRegistry.getInstance().reg(
                context,
                newMethodInvoke(clazz, methodName)
        );
    }

    private void reg(String context, Class<?> clazz) throws Exception {
        for (Method method : clazz.getDeclaredMethods()) {
            DestInvokeIdRegistry.getInstance().reg(
                    context,
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
