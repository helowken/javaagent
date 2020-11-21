package agent.builtin.tools.result;

import static agent.base.utils.AssertUtils.assertEquals;

public class StackTraceCountItem {
    final String name;
    int count = 0;

    StackTraceCountItem(String name) {
        this.name = name;
    }

    void increase() {
        count += 1;
    }

    boolean isValid() {
        return count > 0;
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
