package agent.common.message.command.impl;

import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import static agent.common.message.MessageType.CMD_TEST_CONFIG_BY_RULE;
import static agent.common.message.MessageType.CMD_TRANSFORM_BY_RULE;

public abstract class ByRuleCommand extends AbstractCommand<DefaultStruct> {
    ByRuleCommand(int cmdType, String context, String className) {
        super(cmdType, Structs.newStringArray());
        getBody().set(new String[]{context, className});
    }

    public String getContext() {
        return getConfig()[0];
    }

    public String getClassName() {
        return getConfig()[1];
    }

    private String[] getConfig() {
        return getBody().get();
    }

    public static class TestConfigByRuleCommand extends ByRuleCommand {
        public TestConfigByRuleCommand() {
            this(null, null);
        }

        public TestConfigByRuleCommand(String context, String className) {
            super(CMD_TEST_CONFIG_BY_RULE, context, className);
        }
    }

    public static class TransformByRuleCommand extends ByRuleCommand {
        public TransformByRuleCommand() {
            this(null, null);
        }

        public TransformByRuleCommand(String context, String className) {
            super(CMD_TRANSFORM_BY_RULE, context, className);
        }
    }
}
