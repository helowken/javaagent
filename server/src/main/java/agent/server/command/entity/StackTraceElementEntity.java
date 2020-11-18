package agent.server.command.entity;

import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;

import static agent.server.command.entity.StackTraceElementEntity.TYPE;

@PojoClass(type = TYPE)
public class StackTraceElementEntity {
    public static final int TYPE = 2;
    @PojoProperty(index = 0)
    private int classId;
    @PojoProperty(index = 1)
    private int methodId;

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }
}
