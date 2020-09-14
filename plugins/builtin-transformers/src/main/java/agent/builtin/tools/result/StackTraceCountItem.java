package agent.builtin.tools.result;

import static agent.base.utils.AssertUtils.assertEquals;

public class StackTraceCountItem {
    final String name;
    int count;

    StackTraceCountItem(String name) {
        this.name = name;
        this.count = 1;
    }

    void increase() {
        count += 1;
    }

    void merge(StackTraceCountItem item) {
        assertEquals(name, item.name);
        count += item.count;
    }

    @Override
    public String toString() {
        return name + ": " + count;
    }
}
