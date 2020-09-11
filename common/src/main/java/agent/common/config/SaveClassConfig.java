package agent.common.config;

import static agent.base.utils.AssertUtils.assertNotNull;

public class SaveClassConfig implements ValidConfig {
    private ClassFilterConfig classFilterConfig;
    private boolean withSubClasses;
    private boolean withSubTypes;
    private String outputPath;

    @Override
    public void validate() {
        assertNotNull(outputPath);
    }

    public ClassFilterConfig getClassFilterConfig() {
        return classFilterConfig;
    }

    public void setClassFilterConfig(ClassFilterConfig classFilterConfig) {
        this.classFilterConfig = classFilterConfig;
    }

    public boolean isWithSubClasses() {
        return withSubClasses;
    }

    public void setWithSubClasses(boolean withSubClasses) {
        this.withSubClasses = withSubClasses;
    }

    public boolean isWithSubTypes() {
        return withSubTypes;
    }

    public void setWithSubTypes(boolean withSubTypes) {
        this.withSubTypes = withSubTypes;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

}
