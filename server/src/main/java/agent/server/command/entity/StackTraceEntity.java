package agent.server.command.entity;

import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;

import static agent.server.command.entity.StackTraceEntity.TYPE;

@PojoClass(type = TYPE)
public class StackTraceEntity {
    public static final int TYPE = 1;
    @PojoProperty(index = 0)
    private long threadId;
    @PojoProperty(index = 1)
    private int nameId;

    public int getNameId() {
        return nameId;
    }

    public void setNameId(int nameId) {
        this.nameId = nameId;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

}
