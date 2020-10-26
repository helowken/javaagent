package agent.common.command;

import agent.common.buffer.BufferAllocator;
import agent.common.buffer.ByteUtils;
import agent.common.config.ClassFilterConfig;
import agent.common.config.ResetConfig;
import agent.common.config.TargetConfig;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.command.impl.PojoCommand;
import agent.common.message.command.impl.StringCommand;
import agent.common.struct.BBuff;
import agent.common.struct.DefaultBBuff;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

import static agent.common.message.MessageType.*;
import static org.junit.Assert.*;

public class CommandTest {
    @Test
    public void test() throws Exception {
        ResetConfig resetConfig = new ResetConfig();
        assertEquals(
                resetConfig,
                doTest(new PojoCommand(CMD_RESET, resetConfig)).getPojo()
        );

        TargetConfig targetConfig = new TargetConfig();
        ClassFilterConfig classFilterConfig = new ClassFilterConfig();
        classFilterConfig.setIncludes(Collections.singleton("*.AAA"));
        classFilterConfig.setExcludes(Collections.singleton("*.BB"));
        targetConfig.setClassFilter(classFilterConfig);
        resetConfig.setTargetConfig(targetConfig);
        assertEquals(
                resetConfig,
                doTest(new PojoCommand(CMD_RESET, resetConfig)).getPojo()
        );

        assertEquals(
                "aaa",
                doTest(new StringCommand(CMD_ECHO, "aaa")).getContent()
        );
        assertNull(
                doTest(new StringCommand(CMD_FLUSH_LOG)).getContent()
        );

    }

    private <T extends Command> T doTest(T cmd) {
        ByteBuffer bb = BufferAllocator.allocate(cmd.bytesSize());
        BBuff buff = new DefaultBBuff(bb);
        cmd.serialize(buff);
        byte[] bs = ByteUtils.getBytes(bb);
        bb = ByteBuffer.wrap(bs);

        Command cmd2 = MessageMgr.parse(bb);
        ByteBuffer bb2 = BufferAllocator.allocate(cmd.bytesSize());
        buff = new DefaultBBuff(bb2);
        cmd2.serialize(buff);
        byte[] bs2 = ByteUtils.getBytes(bb2);
        assertTrue(Arrays.equals(bs, bs2));
        return (T) cmd2;
    }
}
