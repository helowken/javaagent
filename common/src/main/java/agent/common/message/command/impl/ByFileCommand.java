package agent.common.message.command.impl;

import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import static agent.common.message.MessageType.CMD_TEST_CONFIG_BY_FILE;
import static agent.common.message.MessageType.CMD_TRANSFORM_BY_FILE;

public class ByFileCommand extends AbstractCommand<DefaultStruct> {

    ByFileCommand(int cmdType, byte[] bs) {
        super(cmdType, Structs.newByteArray());
        getBody().set(bs);
    }

    public byte[] getConfig() {
        return getBody().get();
    }

    public static class TestConfigByFileCommand extends ByFileCommand {
        public TestConfigByFileCommand() {
            this(null);
        }

        public TestConfigByFileCommand(byte[] bs) {
            super(CMD_TEST_CONFIG_BY_FILE, bs);
        }
    }

    public static class TransformByFileCommand extends ByFileCommand {
        public TransformByFileCommand() {
            this(null);
        }

        public TransformByFileCommand(byte[] bs) {
            super(CMD_TRANSFORM_BY_FILE, bs);
        }
    }
}
