package agent.common.message.command.impl;

import agent.common.message.MessageType;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

public class ClasspathCommand extends AbstractCommand<DefaultStruct> {
    public static final String ACTION_ADD = "addCP";
    public static final String ACTION_REMOVE = "removeCP";

    public ClasspathCommand() {
        this(null, null, null);
    }

    public ClasspathCommand(String action, String context, String url) {
        super(MessageType.CMD_CLASSPATH, Structs.newStringArray());
        getBody().set(new String[]{action, context, url});
    }

    public String getAction() {
        return getArgs()[0];
    }

    public String getContext() {
        return getArgs()[1];
    }

    public String getURL() {
        return getArgs()[2];
    }

    private String[] getArgs() {
        return getBody().get();
    }
}
