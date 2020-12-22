package test.server.transform;

import agent.common.message.command.DefaultCommand;
import agent.jvmti.JvmtiUtils;
import agent.server.command.executor.CmdExecutorMgr;
import agent.server.transform.TransformMgr;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static agent.common.message.MessageType.CMD_JS_EXEC;

public class JavascriptExecCmdExecutorTest extends AbstractTest {
    @Test
    public void test() {
        String className = Date.class.getName();
        String s = "$.fields('" + className + "')";
        Object o = CmdExecutorMgr.exec(
                new DefaultCommand(CMD_JS_EXEC, s)
        ).getContent();
        ((Map)o).forEach(
                (k, v) -> System.out.println(k + ": " + v)
        );

        s = "$.methods('" + className + "')";
         o = CmdExecutorMgr.exec(
                new DefaultCommand(CMD_JS_EXEC, s)
        ).getContent();
        ((Map)o).forEach(
                (k, v) -> System.out.println(k + ": " + v)
        );

        s = "$.clsInfo('" + className + "')";
        o = CmdExecutorMgr.exec(
                new DefaultCommand(CMD_JS_EXEC, s)
        ).getContent();
        ((Map)o).forEach(
                (k, v) -> System.out.println(k + ": " + v)
        );
//        System.out.println(o);
    }
}
