package agent.server.transform.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class AroundItem<T, R> {
    private int seq = 0;
    private Stack<T> undergoing = new Stack<>();
    private LinkedList<R> completed = new LinkedList<>();

    void push(T data) {
        undergoing.push(data);
    }

    public int nextSeq() {
        return ++seq;
    }

    T peek() {
        if (undergoing.isEmpty())
            return null;
        return undergoing.peek();
    }

    public int size() {
        return undergoing.size();
    }

    void complete(ProcessDataFunc<T, R> func, boolean peek, boolean addToLast) {
        if (!undergoing.isEmpty() && func != null) {
            T data = peek ? undergoing.peek() : undergoing.pop();
            R result = func.process(data);
            if (result != null) {
                if (addToLast)
                    completed.addLast(result);
                else
                    completed.addFirst(result);
            }
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
