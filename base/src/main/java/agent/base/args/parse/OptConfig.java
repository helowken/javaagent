package agent.base.args.parse;

public class OptConfig {
    private static final String PREFIX = "-";
    private static final String PREFIX2 = "--";
    private final String name;
    private final String fullName;
    private final String key;
    private final String desc;
    private final OptValueType valueType;
    private final boolean allowMulti;

    public static boolean isOpt(String arg) {
        if (arg == null)
            throw new IllegalArgumentException();
        return arg.startsWith(PREFIX) || arg.startsWith(PREFIX2);
    }

    public OptConfig(String name, String key) {
        this(name, null, key, null);
    }

    public OptConfig(String name, String fullName, String key, String desc) {
        this(name, fullName, key, desc, OptValueType.STRING, false);
    }

    public OptConfig(String name, String fullName, String key, String desc, OptValueType valueType, boolean allowMulti) {
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
        this.desc = desc;
        this.valueType = valueType;
        this.allowMulti = allowMulti;
    }

    String getDisplayName() {
        String s = fullName;
        if (s == null)
            s = name;
        else if (name != null)
            s += ", " + name;
        return s;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public OptValueType getValueType() {
        return valueType;
    }

    public boolean isAllowMulti() {
        return allowMulti;
    }

    public boolean match(String arg) {
        return arg.equals(name) || arg.equals(fullName);
    }
}
