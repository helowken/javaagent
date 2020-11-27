package agent.common.tree;

import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static agent.common.tree.Node.POJO_TYPE;

@PojoClass(type=POJO_TYPE)
public class Node<T> implements INode<T, Node<T>> {
    public static final int POJO_TYPE = 1000;
    private Node<T> parent;
    @PojoProperty(index = 1)
    private T data;
    @PojoProperty(index = 2)
    private LinkedList<Node<T>> children = new LinkedList<>();
    private Map<String, Object> userProps = new HashMap<>();

    public Node() {
    }

    public Node(T data) {
        this.setData(data);
    }

    public void refreshParent() {
        children.forEach(
                child -> {
                    child.parent = this;
                    child.refreshParent();
                }
        );
    }

    @Override
    public Node<T> getParent() {
        return parent;
    }

    @Override
    public List<Node<T>> getChildren() {
        return new LinkedList<>(children);
    }

    public void setChildren(LinkedList<Node<T>> children) {
        this.children = children;
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
    public void reverseChildren() {
        Collections.reverse(children);
    }

    @Override
    public Node<T> addChildAt(int idx, Node<T> child) {
        children.add(idx, child);
        child.parent = this;
        return child;
    }

    @Override
    public Node<T> appendChild(Node<T> child) {
        if (child.parent != null)
            child.parent.removeChild(child);
        children.add(child);
        child.parent = this;
        return child;
    }

    @Override
    public void appendChildren(Node<T> node) {
        node.getChildren().forEach(this::appendChild);
    }

    @Override
    public void removeChild(Node<T> child) {
        if (children.remove(child))
            child.parent = null;
    }

    @Override
    public void removeChildAt(int idx) {
        Node<T> child = children.remove(idx);
        if (child != null)
            child.parent = null;
    }

    @Override
    public void removeAll(boolean destroy) {
        if (destroy) {
            getChildren().forEach(Node::destroy);
        } else {
            children.forEach(child -> child.parent = null);
            children.clear();
        }
    }

    @Override
    public Node<T> lastChild() {
        return children.isEmpty() ? null : children.getLast();
    }

    @Override
    public Node<T> firstChild() {
        return children.isEmpty() ? null : children.getFirst();
    }

    @Override
    public List<Node<T>> findChildren(Predicate<T> predicate) {
        return children.stream()
                .filter(
                        child -> predicate.test(child.data)
                )
                .collect(
                        Collectors.toList()
                );
    }

    @Override
    public Node<T> findFirstChild(Predicate<T> predicate) {
        return children.stream()
                .filter(
                        child -> predicate.test(child.data)
                )
                .findFirst()
                .orElse(null);
    }

    @Override
    public Node<T> getChildAt(int idx) {
        if (idx < 0 || idx >= countChildren())
            throw new IllegalArgumentException("Invalid index: " + idx);
        return children.get(idx);
    }

    @Override
    public int indexOf(Node<T> child) {
        return children.indexOf(child);
    }

    @Override
    public boolean isFirstChild(Node<T> child) {
        return !children.isEmpty() && children.getFirst() == child;
    }

    @Override
    public boolean isLastChild(Node<T> child) {
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

    public void setUserProp(String key, Object value) {
        userProps.put(key, value);
    }

    public Object getUserProp(String key) {
        return userProps.get(key);
    }

    @Override
    public String toString() {
        return "" + data;
    }
}
