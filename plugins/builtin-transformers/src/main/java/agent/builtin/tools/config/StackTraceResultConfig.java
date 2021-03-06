package agent.builtin.tools.config;

import agent.common.config.StackTraceConfig;

import java.util.Map;

public class StackTraceResultConfig extends AbstractResultConfig {
    private StackTraceConfig stackTraceConfig;
    private float rate;
    private String outputFormat;
    private Map<Integer, Boolean> numMap;

    public StackTraceConfig getStackTraceConfig() {
        return stackTraceConfig;
    }

    public void setStackTraceConfig(StackTraceConfig stackTraceConfig) {
        this.stackTraceConfig = stackTraceConfig;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Map<Integer, Boolean> getNumMap() {
        return numMap;
    }

    public void setNumMap(Map<Integer, Boolean> numMap) {
        this.numMap = numMap;
    }
}
