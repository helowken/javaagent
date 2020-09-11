package agent.common.config;

import java.util.Map;

public class StackTraceConfig extends AbstractScheduleConfig {
    private String outputPath;
    private Map<String, Object> logConfig;

    @Override
    public void validate() {
        super.validate();
        validateNotBlank(outputPath, "Output path");
        validateNotNull(logConfig, "Log config");
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public Map<String, Object> getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(Map<String, Object> logConfig) {
        this.logConfig = logConfig;
    }
}
