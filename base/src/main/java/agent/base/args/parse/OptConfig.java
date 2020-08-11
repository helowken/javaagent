package agent.base.args.parse;

import agent.base.utils.Utils;

public class OptConfig {
    private static final String PREFIX = "-";
    private static final String PREFIX2 = "--";
    private final String name;
    private final String fullName;
    private final String key;
    private final OptValueType valueType;
    private final boolean allowMulti;

    static boolean isOpt(String arg) {
        if (arg == null)
            throw new IllegalArgumentException();
        return arg.startsWith(PREFIX) || arg.startsWith(PREFIX2);
    }

    public OptConfig(String name, String key) {
        this(name, null, key);
    }

    public OptConfig(String name, String fullName, String key) {
        this(name, fullName, key, OptValueType.STRING, false);
    }

    public OptConfig(String name, String fullName, String key, OptValueType valueType, boolean allowMulti) {
        if (name == null && fullName == null)
            throw new IllegalArgumentException("Name and full name can't be both null.");
        if (name != null && !name.startsWith(PREFIX))
            throw new IllegalArgumentException("Invalid name: " + name);
        if (fullName != null && !fullName.startsWith(PREFIX2))
            throw new IllegalArgumentException("Invalid full name: " + fullName);
        if (key == null)
            throw new IllegalArgumentException("Invalid key.");
        if (valueType == null)
            throw new IllegalArgumentException("Invalid valueType.");
        this.name = name;
        this.fullName = fullName;
        this.key = key;
        this.valueType = valueType;
        this.allowMulti = allowMulti;
    }

    String getDisplayName() {
        return Utils.isBlank(fullName) ? name : fullName;
    }

    String getName() {
        return name;
    }

    String getFullName() {
        return fullName;
    }

    String getKey() {
        return key;
    }

    OptValueType getValueType() {
        return valueType;
    }

    boolean isAllowMulti() {
        return allowMulti;
    }

    boolean match(String arg) {
        return arg.equals(name) || arg.equals(fullName);
    }
}
