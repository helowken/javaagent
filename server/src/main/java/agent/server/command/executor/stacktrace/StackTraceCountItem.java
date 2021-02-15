package agent.server.command.executor.stacktrace;

import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;

import static agent.server.command.executor.stacktrace.StackTraceCountItem.POJO_TYPE;

@PojoClass(type = POJO_TYPE)
public class StackTraceCountItem {
    public static final int POJO_TYPE = 2;
    @PojoProperty(index = 1)
    private int classId;
    @PojoProperty(index = 2)
    private int methodId;
    @PojoProperty(index = 3)
    private int count = 0;
    @PojoProperty(index = 4)
    private int id;

    public StackTraceCountItem() {
    }

    StackTraceCountItem(int id, int classId, int methodId) {
        this.id = id;
        this.classId = classId;
        this.methodId = methodId;
    }

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    void increase() {
        count += 1;
    }

    public void add(int v) {
        count += v;
    }

    public boolean isValid() {
        return count > 0;
    }
}
