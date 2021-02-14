package agent.builtin.tools.result.data;

import agent.server.command.executor.stacktrace.StackTraceCountItem;

import static agent.base.utils.AssertUtils.assertEquals;

public class StackTraceResultItem extends StackTraceCountItem {
    private static int idGen = 0;
    private int totalCount = 0;
    private int id = ++idGen;

    public int getId() {
        return id;
    }

    @Override
    public void setCount(int count) {
        super.setCount(count);
        totalCount = count;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void add(int v) {
        this.totalCount += v;
    }

    public boolean isValid() {
        return totalCount > 0;
    }

    public void merge(StackTraceResultItem item) {
        assertEquals(classId, item.classId);
        assertEquals(methodId, item.methodId);
        count += item.count;
        totalCount += item.totalCount;
    }
}
