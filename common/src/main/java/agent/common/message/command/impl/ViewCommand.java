package agent.common.message.command.impl;

import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import java.util.HashSet;
import java.util.Set;

import static agent.common.message.MessageType.CMD_VIEW;

public class ViewCommand extends AbstractCommand<DefaultStruct> {
    public static final String CATALOG_CLASS = "class";
    private static final Set<String> catalogSet = new HashSet<>();

    static {
        catalogSet.add(CATALOG_CLASS);
    }

    public ViewCommand() {
        super(CMD_VIEW, Structs.newStringArray());
    }

    public ViewCommand(String catalog) {
        this();
        getBody().set(new String[]{catalog});
    }

    public String getCatalog() {
        String[] args = getBody().get();
        if (args != null && args.length > 0)
            return args[0];
        throw new RuntimeException("No catalog found.");
    }

    public static boolean isCatalogValid(String catalog) {
        return catalogSet.contains(catalog);
    }
}
