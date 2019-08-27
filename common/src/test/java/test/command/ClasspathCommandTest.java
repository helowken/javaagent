package test.command;

import agent.common.buffer.BufferAllocator;
import agent.common.message.command.impl.ClasspathCommand;
import org.junit.Test;

import java.net.URL;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class ClasspathCommandTest {
    @Test
    public void test() throws Exception {
        doTest(ClasspathCommand.ACTION_ADD, "test", "http://localhost:8008/");
        doTest(ClasspathCommand.ACTION_REMOVE, "test2", "file:///home/xxx/");
    }

    private void doTest(String action, String context, String url) throws Exception {
        ClasspathCommand cmd = new ClasspathCommand(
                action, context, new URL(url).toString()
        );

        ByteBuffer bb = BufferAllocator.allocate(cmd.bytesSize());
        cmd.serialize(bb);

        bb.flip();
        ClasspathCommand cmd2 = new ClasspathCommand();
        cmd2.deserialize(bb);

        assertEquals(action, cmd2.getAction());
        assertEquals(context, cmd2.getContext());
        assertEquals(new URL(url), new URL(cmd2.getURL()));
    }
}
