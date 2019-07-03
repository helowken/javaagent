package agent.common.message.command.impl;

import agent.common.message.command.CommandType;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

public class EchoCommand extends AbstractCommand<DefaultStruct> {
    private static final int cmdType = CommandType.CMD_TYPE_ECHO;

    public EchoCommand() {
        this(null);
    }

    public EchoCommand(String content) {
        super(cmdType, Structs.newString());
        getBody().set(content);
    }

    public String getContent() {
        return getBody().get();
    }
}
