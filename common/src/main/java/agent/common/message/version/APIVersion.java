package agent.common.message.version;

import agent.common.struct.impl.MapStruct;

public class APIVersion extends MapStruct<String, Integer> {
    private static final String FIELD_MAJOR_VERSION = "majorVersion";
    private static final String FIELD_MINOR_VERSION = "minorVersion";
    private static final APIVersion instance = new APIVersion(1, 0);

    public static APIVersion getInstance() {
        return instance;
    }

    public APIVersion(int majorVersion, int minorVersion) {
        put(FIELD_MAJOR_VERSION, majorVersion);
        put(FIELD_MINOR_VERSION, minorVersion);
    }

    public int getMajorVersion() {
        return get(FIELD_MAJOR_VERSION);
    }

    public int getMinorVersion() {
        return get(FIELD_MINOR_VERSION);
    }

    @Override
    public String toString() {
        return getMajorVersion() + "." + getMinorVersion();
    }
}
