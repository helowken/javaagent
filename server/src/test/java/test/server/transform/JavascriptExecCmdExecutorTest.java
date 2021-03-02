package test.server.transform;

import agent.cmdline.command.DefaultCommand;
import agent.server.command.executor.ServerCmdExecMgr;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Date;
import java.util.Map;

import static agent.common.message.MessageType.CMD_JS_EXEC;

public class JavascriptExecCmdExecutorTest extends AbstractTest {
    @Test
    public void test() {
        String className = Date.class.getName();
        String s = "$.fields('" + className + "')";
        Object o = ServerCmdExecMgr.exec(
                new DefaultCommand(CMD_JS_EXEC, s)
        ).getContent();
        ((Map)o).forEach(
                (k, v) -> System.out.println(k + ": " + v)
        );

        s = "$.methods('" + className + "')";
         o = ServerCmdExecMgr.exec(
                new DefaultCommand(CMD_JS_EXEC, s)
        ).getContent();
         System.out.println("=========: " + o);
        ((Map)o).forEach(
                (k, v) -> System.out.println(k + ": " + v)
        );

        s = "$.clsInfo('" + className + "')";
        o = ServerCmdExecMgr.exec(
                new DefaultCommand(CMD_JS_EXEC, s)
        ).getContent();
        ((Map)o).forEach(
                (k, v) -> System.out.println(k + ": " + v)
        );
//        System.out.println(o);
    }
}
