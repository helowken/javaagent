package agent.common.command;

import org.junit.Test;
import agent.common.buffer.BufferAllocator;
import agent.common.buffer.ByteUtils;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.command.impl.EchoCommand;
import agent.common.message.command.impl.ResetCommand;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class CommandTest {
    @Test
    public void test() throws Exception {
        doTest(new ResetCommand());
        doTest(new EchoCommand("aaa"));
    }

    private void doTest(Command cmd) throws Exception {
        ByteBuffer bb = BufferAllocator.allocate(cmd.bytesSize());
        cmd.serialize(bb);
        byte[] bs = ByteUtils.getBytes(bb);
        bb = ByteBuffer.wrap(bs);
        Command cmd2 = MessageMgr.parseCommand(bb);
        ByteBuffer bb2 = BufferAllocator.allocate(cmd.bytesSize());
        cmd2.serialize(bb2);
        byte[] bs2 = ByteUtils.getBytes(bb2);
        assertTrue(Arrays.equals(bs, bs2));
    }
}
