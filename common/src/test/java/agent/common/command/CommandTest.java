package agent.common.command;

import agent.common.buffer.BufferAllocator;
import agent.common.buffer.ByteUtils;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.command.impl.EchoCommand;
import agent.common.message.command.impl.MapCommand;
import agent.common.struct.BBuff;
import agent.common.struct.DefaultBBuff;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static agent.common.message.MessageType.CMD_RESET;
import static org.junit.Assert.assertTrue;

public class CommandTest {
    @Test
    public void test() throws Exception {
        doTest(new MapCommand(CMD_RESET));
        doTest(new EchoCommand("aaa"));
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
