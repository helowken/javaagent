package agent.common.message.command.impl;

import agent.common.message.MessageType;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import java.util.HashSet;
import java.util.Set;

public class ClasspathCommand extends AbstractCommand<DefaultStruct> {
    public static final String ACTION_ADD = "add";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REFRESH = "refresh";
    private static final Set<String> actions = new HashSet<>();

    static {
        actions.add(ACTION_ADD);
        actions.add(ACTION_REFRESH);
        actions.add(ACTION_REMOVE);
    }

    public ClasspathCommand() {
        this(null, null, null);
    }

    public ClasspathCommand(String context, String action, String url) {
        super(MessageType.CMD_CLASSPATH, Structs.newStringArray());
        if (context != null) {
            getBody().set(new String[]{context, action, url});
        }
    }

    public String getContext() {
        return getArgs()[0];
    }

    public String getAction() {
        return getArgs()[1];
    }

    public String getURL() {
        return getArgs()[2];
    }

    private String[] getArgs() {
        return getBody().get();
    }

    public static boolean isActionValid(String action) {
        return actions.contains(action);
    }
}
