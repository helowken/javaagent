package agent.common.message.version;

import agent.common.struct.impl.annotation.PojoProperty;

public class ApiVersion {
    private static final ApiVersion instance = create();
    @PojoProperty(index = 0)
    private int majorVersion;
    @PojoProperty(index = 1)
    private int minorVersion;

    public static ApiVersion getDefault() {
        return instance;
    }

    private static ApiVersion create() {
        ApiVersion apiVersion = new ApiVersion();
        apiVersion.setMajorVersion(1);
        apiVersion.setMinorVersion(0);
        return apiVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    @Override
    public String toString() {
        return minorVersion + ":" + minorVersion;
    }
}
