package agent.server.command.entity;

import agent.common.utils.annotation.PojoProperty;

public class StackTraceElementEntity {
    @PojoProperty(index = 0)
    private String className;
    @PojoProperty(index = 1)
    private String methodName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
