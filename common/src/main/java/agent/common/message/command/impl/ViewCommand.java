package agent.common.message.command.impl;

import agent.base.utils.Utils;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static agent.common.message.MessageType.CMD_VIEW;

public class ViewCommand extends AbstractCommand<DefaultStruct> {
    public static final String SEP = "=";
    public static final String CATALOG_CONTEXT = "context";
    public static final String CATALOG_CLASS = "class";
    public static final String CATALOG_INVOKE = "invoke";
    public static final String CATALOG_PROXY = "proxy";
    private static final Set<String> catalogSet = new HashSet<>();

    static {
        catalogSet.add(CATALOG_CONTEXT);
        catalogSet.add(CATALOG_CLASS);
        catalogSet.add(CATALOG_INVOKE);
        catalogSet.add(CATALOG_PROXY);
    }

    public ViewCommand() {
        super(CMD_VIEW, Structs.newStringArray());
    }

    public ViewCommand(String... args) {
        this();
        if (args == null || args.length == 0)
            throw new IllegalArgumentException("Invalid args!");
        getBody().set(args);
    }

    public String getCatalog() {
        String[] args = getBody().get();
        if (args != null && args.length > 0) {
            validateCatalog(args[0]);
            return args[0];
        }
        throw new RuntimeException("No catalog found.");
    }

    public Map<String, String> getFilterMap() {
        Map<String, String> filterMap = new HashMap<>();
        String[] args = getBody().get();
        if (args != null && args.length > 1) {
            for (int i = 1; i < args.length; ++i) {
                String[] ts = validateFilter(args[i]);
                filterMap.put(ts[0], ts[1]);
            }
        }
        return filterMap;
    }

    public static void validateCatalog(String catalog) {
        if (!catalogSet.contains(catalog))
            throw new IllegalArgumentException("Unknown catalog: " + catalog);
    }

    public static String[] validateFilter(String filter) {
        String[] ts = filter.split(SEP);
        if (ts.length != 2 || Utils.isBlank(ts[1]))
            throw new IllegalArgumentException("Invalid filter: " + filter);
        validateCatalog(ts[0]);
        return ts;
    }
}
