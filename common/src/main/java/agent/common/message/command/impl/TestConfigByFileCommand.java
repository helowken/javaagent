package agent.common.message.command.impl;

import agent.common.message.MessageType;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

public class TestConfigByFileCommand extends AbstractCommand<DefaultStruct> {

    public TestConfigByFileCommand() {
        this(null);
    }

    public TestConfigByFileCommand(byte[] bs) {
        super(MessageType.CMD_TEST_CONFIG_BY_FILE, Structs.newByteArray());
        getBody().set(bs);
    }

    public byte[] getConfig() {
        return getBody().get();
    }
}
