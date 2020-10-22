package agent.common.command;

import agent.common.buffer.BufferAllocator;
import agent.common.buffer.ByteUtils;
import agent.common.config.ResetConfig;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.command.impl.PojoCommand;
import agent.common.message.command.impl.StringCommand;
import agent.common.struct.BBuff;
import agent.common.struct.DefaultBBuff;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static agent.common.message.MessageType.CMD_ECHO;
import static agent.common.message.MessageType.CMD_RESET;
import static org.junit.Assert.assertTrue;

public class CommandTest {
    @Test
    public void test() throws Exception {
        doTest(new PojoCommand(CMD_RESET, new ResetConfig()));
        doTest(new StringCommand(CMD_ECHO, "aaa"));
    }

    private void doTest(Command cmd) throws Exception {
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
    }
}
