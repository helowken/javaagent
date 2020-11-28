package agent.server.command.executor.stacktrace;

import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;

import static agent.base.utils.AssertUtils.assertEquals;
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

    public StackTraceCountItem() {
    }

    StackTraceCountItem(int classId, int methodId) {
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

    public void increase() {
        count += 1;
    }

    public void add(int v) {
        this.count += v;
    }

    public boolean isValid() {
        return count > 0;
    }

    public void merge(StackTraceCountItem item) {
        assertEquals(classId, item.classId);
        assertEquals(methodId, item.methodId);
        count += item.count;
    }
}
