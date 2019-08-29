package agent.common.message.command.impl;

import agent.common.message.MessageType;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

public class TestConfigByRuleCommand extends AbstractCommand<DefaultStruct> {

    public TestConfigByRuleCommand() {
        this(null);
    }

    public TestConfigByRuleCommand(String className) {
        super(MessageType.CMD_TEST_CONFIG_BY_RULE, Structs.newString());
        getBody().set(className);
    }

    public String getConfig() {
        return getBody().get();
    }
}
