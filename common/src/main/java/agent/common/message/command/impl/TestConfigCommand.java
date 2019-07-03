package agent.common.message.command.impl;

import agent.common.message.command.CommandType;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

public class TestConfigCommand extends AbstractCommand<DefaultStruct> {

    public TestConfigCommand() {
        this(null);
    }

    public TestConfigCommand(byte[] bs) {
        super(CommandType.CMD_TYPE_TEST_CONFIG, Structs.newByteArray());
        getBody().set(bs);
    }

    public byte[] getConfig() {
        return getBody().get();
    }
}
