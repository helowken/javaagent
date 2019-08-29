package agent.common.message.command.impl;

import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import static agent.common.message.MessageType.CMD_TRANSFORM_BY_FILE;

public class TransformByFileCommand extends AbstractCommand<DefaultStruct> {
    public TransformByFileCommand() {
        this(null);
    }

    public TransformByFileCommand(byte[] bs) {
        super(CMD_TRANSFORM_BY_FILE, Structs.newByteArray());
        getBody().set(bs);
    }

    public byte[] getConfig() {
        return getBody().get();
    }

}
