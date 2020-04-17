package agent.common.message.command.impl;

import agent.common.message.MessageType;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import java.util.*;
import java.util.stream.Collectors;

public class ResetCommand extends AbstractCommand<DefaultStruct> {
    public ResetCommand() {
        this(null);
    }

    public ResetCommand(String... classExprSet) {
        super(MessageType.CMD_RESET, Structs.newStringArray());
        List<String> args = new ArrayList<>();
        if (classExprSet != null)
            args.addAll(
                    Arrays.stream(classExprSet)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toSet())
            );
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
