package agent.builtin.tools.config;

import agent.builtin.tools.result.filter.ResultFilter;

public abstract class AbstractResultConfig {
    private String inputPath;
    private boolean shortName;
    private ResultFilter filter;

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public boolean isShortName() {
        return shortName;
    }

    public void setShortName(boolean shortName) {
        this.shortName = shortName;
    }

    public <T> ResultFilter<T> getFilter() {
        return filter;
    }

    public void setFilter(ResultFilter filter) {
        this.filter = filter;
    }
}
