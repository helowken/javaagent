package agent.common.message.command;

import agent.common.struct.impl.annotation.PojoProperty;

public class DefaultCommand implements Command {
    @PojoProperty(index = 0)
    private int type;
    @PojoProperty(index = 1)
    private Object content;

    public DefaultCommand() {
    }

    public DefaultCommand(int type, Object content) {
        this.setType(type);
        this.setContent(content);
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public <T> T getContent() {
        return (T) content;
    }
}
