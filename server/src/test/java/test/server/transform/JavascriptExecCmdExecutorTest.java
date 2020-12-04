package test.server.transform;

import agent.common.message.command.DefaultCommand;
import agent.server.command.executor.CmdExecutorMgr;
import agent.server.transform.TransformMgr;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Collection;
import java.util.Map;

import static agent.common.message.MessageType.CMD_JS_EXEC;

public class JavascriptExecCmdExecutorTest extends AbstractTest {
    @Test
    public void test() {
        String className = TransformMgr.getInstance().getClass().getName();
        String s = "$.fields('" + className + "')";
        Object o = CmdExecutorMgr.exec(
                new DefaultCommand(CMD_JS_EXEC, s)
        ).getContent();
        ((Collection)o).forEach(System.out::println);

        s = "$.methods('" + className + "')";
         o = CmdExecutorMgr.exec(
                new DefaultCommand(CMD_JS_EXEC, s)
        ).getContent();
        ((Collection)o).forEach(System.out::println);

        s = "$.clsInfo('" + className + "')";
        o = CmdExecutorMgr.exec(
                new DefaultCommand(CMD_JS_EXEC, s)
        ).getContent();
        ((Collection)o).forEach(System.out::println);
//        System.out.println(o);
    }
}
