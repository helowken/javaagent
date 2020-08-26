package agent.common.config;

public class InfoQuery {
    public static final int INFO_CLASS = 0;
    public static final int INFO_INVOKE = 1;
    public static final int INFO_PROXY = 2;

    private int level;
    private TargetConfig targetConfig;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public TargetConfig getTargetConfig() {
        return targetConfig;
    }

    public void setTargetConfig(TargetConfig targetConfig) {
        this.targetConfig = targetConfig;
    }

    @Override
    public String toString() {
        return "InfoQuery{" +
                "level=" + level +
                ", targetConfig=" + targetConfig +
                '}';
    }
}
