package agent.common.message.command.impl;

import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

public class StringCommand extends AbstractCommand<DefaultStruct> {

    public StringCommand(int type) {
        this(type, null);
    }

    public StringCommand(int type, String content) {
        super(type, Structs.newString());
        getBody().set(content);
    }

    public String getContent() {
        return getBody().get();
    }
}
