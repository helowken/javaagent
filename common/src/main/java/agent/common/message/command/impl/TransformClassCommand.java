package agent.common.message.command.impl;

import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import static agent.common.message.MessageType.CMD_TRANSFORM_CLASS;

public class TransformClassCommand extends AbstractCommand<DefaultStruct> {
    public TransformClassCommand() {
        this(null);
    }

    public TransformClassCommand(byte[] bs) {
        super(CMD_TRANSFORM_CLASS, Structs.newByteArray());
        getBody().set(bs);
    }

    public byte[] getConfig() {
        return getBody().get();
    }

}
