package agent.common.message.command.impl;

import agent.common.message.command.CommandType;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import java.util.*;

public class ResetClassCommand extends AbstractCommand<DefaultStruct> {
    public ResetClassCommand() {
        this(null);
    }

    public ResetClassCommand(String contextExpr, String... classExprSet) {
        super(CommandType.CMD_TYPE_RESET_CLASS, Structs.newStringArray());
        List<String> args = new ArrayList<>();
        if (contextExpr != null) {
            args.add(contextExpr);
            if (classExprSet != null)
                args.addAll(Arrays.asList(classExprSet));
        }
        getBody().set(args.toArray(new String[0]));
    }

    public String getContextExpr() {
        String[] args = getArgs();
        if (args != null && args.length > 0)
            return args[0];
        return null;
    }

    public Set<String> getClassExprSet() {
        Set<String> classExprSet = new HashSet<>();
        String[] args = getArgs();
        if (args != null && args.length > 1) {
            List<String> argList = Arrays.asList(args);
            classExprSet.addAll(argList.subList(1, argList.size()));
        }
        return classExprSet;
    }

    private String[] getArgs() {
        return getBody().get();
    }
}
