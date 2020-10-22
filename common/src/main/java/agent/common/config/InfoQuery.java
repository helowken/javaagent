package agent.common.config;

import agent.common.utils.annotation.PojoProperty;

import java.util.HashSet;
import java.util.Set;

import static agent.base.utils.AssertUtils.assertTrue;

public class InfoQuery extends AbstractValidConfig {
    public static final int INFO_CLASS = 0;
    public static final int INFO_INVOKE = 1;
    public static final int INFO_PROXY = 2;
    private static final Set<Integer> validLevels = new HashSet<>();

    static {
        validLevels.add(INFO_CLASS);
        validLevels.add(INFO_INVOKE);
        validLevels.add(INFO_PROXY);
    }

    @PojoProperty(index = 0)
    private int level;
    @PojoProperty(index = 1)
    private TargetConfig targetConfig;

    @Override
    public void validate() {
        validateNotNull(targetConfig, "Target config");
        assertTrue(
                validLevels.contains(level),
                "Level is invalid: " + level
        );
    }

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
