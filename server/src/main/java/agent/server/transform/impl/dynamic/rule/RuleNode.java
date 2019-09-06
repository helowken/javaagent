package agent.server.transform.impl.dynamic.rule;

import java.util.LinkedList;
import java.util.List;

public class RuleNode<T> implements INode<T, RuleNode<T>> {
    private RuleNode<T> parent;
    private LinkedList<RuleNode<T>> children = new LinkedList<>();
    private T data;

    @Override
    public RuleNode<T> getParent() {
        return parent;
    }

    @Override
    public List<RuleNode<T>> getChildren() {
        return new LinkedList<>(children);
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public void addChildAt(int idx, RuleNode<T> child) {
        children.add(idx, child);
        child.parent = this;
    }

    @Override
    public void appendChild(RuleNode<T> child) {
        children.add(child);
        child.parent = this;
    }

    @Override
    public void removeChild(RuleNode<T> child) {
        children.remove(child);
    }

    @Override
    public void removeChildAt(int idx) {
        children.remove(idx);
    }

    @Override
    public void removeAll(boolean destroy) {
        if (destroy) {
            getChildren().forEach(RuleNode::destroy);
        } else {
            children.forEach(child -> child.parent = null);
            children.clear();
        }
    }

    @Override
    public RuleNode<T> lastChild() {
        return children.isEmpty() ? null : children.getLast();
    }

    @Override
    public RuleNode<T> firstChild() {
        return children.isEmpty() ? null : children.getFirst();
    }

    @Override
    public RuleNode<T> getChildAt(int idx) {
        if (idx < 0 || idx >= countChildren())
            throw new IllegalArgumentException("Invalid index: " + idx);
        return children.get(idx);
    }

    @Override
    public int indexOf(RuleNode<T> child) {
        return children.indexOf(child);
    }

    @Override
    public boolean isFirstChild(RuleNode<T> child) {
        return !children.isEmpty() && children.getFirst() == child;
    }

    @Override
    public boolean isLastChild(RuleNode<T> child) {
        return !children.isEmpty() && children.getLast() == child;
    }

    @Override
    public void destroy() {
        if (this.parent != null)
            this.parent.removeChild(this);
        this.parent = null;
        this.data = null;
        this.removeAll(true);
    }

    @Override
    public int countChildren() {
        return children.size();
    }

    @Override
    public int getLevel() {
        return isRoot() ? 0 : 1 + parent.getLevel();
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public void setData(T data) {
        this.data = data;
    }
}
