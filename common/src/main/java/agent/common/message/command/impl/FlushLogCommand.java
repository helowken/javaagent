package agent.common.message.command.impl;

import agent.common.message.MessageType;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

public class FlushLogCommand extends AbstractCommand<DefaultStruct> {
    public FlushLogCommand() {
        this(null);
    }

    public FlushLogCommand(String outputPath) {
        super(MessageType.CMD_FLUSH_LOG, Structs.newString());
        getBody().set(outputPath);
    }

    public String getOutputPath() {
        return getBody().get();
    }

}
