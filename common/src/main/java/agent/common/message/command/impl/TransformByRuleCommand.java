package agent.common.message.command.impl;

import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import static agent.common.message.MessageType.CMD_TRANSFORM_BY_RULE;

public class TransformByRuleCommand extends AbstractCommand<DefaultStruct> {
    public TransformByRuleCommand() {
        this(null);
    }

    public TransformByRuleCommand(String className) {
        super(CMD_TRANSFORM_BY_RULE, Structs.newString());
        getBody().set(className);
    }

    public String getConfig() {
        return getBody().get();
    }

}
