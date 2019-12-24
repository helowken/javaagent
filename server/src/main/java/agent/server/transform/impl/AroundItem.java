package agent.server.transform.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class AroundItem<T, R> {
    private Stack<T> undergoing = new Stack<>();
    private List<R> completed = new ArrayList<>(100);

    void add(T data) {
        undergoing.push(data);
    }

    public T peek() {
        if (undergoing.isEmpty())
            return null;
        return undergoing.peek();
    }

    public int size() {
        return undergoing.size();
    }

    void complete(ProcessDataFunc<T, R> func) {
        if (!undergoing.isEmpty() && func != null) {
            T data = undergoing.pop();
            R result = func.process(data);
            if (result != null)
                completed.add(result);
        }
    }

    List<R> getCompleted() {
        return Collections.unmodifiableList(completed);
    }

    boolean isCompleted() {
        return undergoing.isEmpty();
    }

    interface ProcessDataFunc<T, R> {
        R process(T data);
    }
}
