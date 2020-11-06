package agent.server.command.entity;

import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;

import static agent.server.command.entity.StackTraceElementEntity.TYPE;

@PojoClass(type = TYPE)
public class StackTraceElementEntity {
    public static final int TYPE = 2;
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
