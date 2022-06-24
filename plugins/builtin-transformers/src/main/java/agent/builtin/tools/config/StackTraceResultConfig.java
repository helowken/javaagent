package agent.builtin.tools.config;

import agent.common.config.StackTraceConfig;

import java.util.Map;

public class StackTraceResultConfig extends AbstractResultConfig {
    private StackTraceConfig stackTraceConfig;
    private float rate;
    private String outputFormat;
    private boolean displayAll;
    private boolean rateAfterFilter;
    private Map<Integer, Integer> numMap;

    public StackTraceConfig getStackTraceConfig() {
        return stackTraceConfig;
    }

    public void setStackTraceConfig(StackTraceConfig stackTraceConfig) {
        this.stackTraceConfig = stackTraceConfig;
    }

    public boolean isDisplayAll() {
        return displayAll;
    }

    public void setDisplayAll(boolean displayAll) {
        this.displayAll = displayAll;
    }

    public boolean isRateAfterFilter() {
        return rateAfterFilter;
    }

    public void setRateAfterFilter(boolean rateOfFilter) {
        this.rateAfterFilter = rateOfFilter;
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

    public Map<Integer, Integer> getNumMap() {
        return numMap;
    }

    public void setNumMap(Map<Integer, Integer> numMap) {
        this.numMap = numMap;
    }
}
