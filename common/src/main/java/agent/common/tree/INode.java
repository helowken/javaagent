package agent.common.tree;

import java.util.List;
import java.util.function.Predicate;

public interface INode<T, N extends INode> {
    N getParent();

    boolean isAncestorOf(N node);

    List<N> getChildren();

    List<N> findChildren(Predicate<T> predicate);

    N findFirstChild(Predicate<T> predicate);

    boolean isLeaf();

    boolean isRoot();

    default INode getRoot() {
        INode pn = getParent();
        return pn == null ? this : pn.getRoot();
    }

    void reverseChildren();

    N addChildAt(int idx, N child);

    N appendChild(N child);

    void appendChildren(N node);

    void removeChild(N child);

    void removeChildAt(int idx);

    void removeAll(boolean destroy);

    N lastChild();

    N firstChild();

    N getChildAt(int idx);

    int indexOf(N child);

    default boolean containsChild(N child) {
        return indexOf(child) > -1;
    }

    boolean isFirstChild(N child);

    boolean isLastChild(N child);

    void destroy();

    int countChildren();

    default boolean hasChild() {
        return countChildren() > 0;
    }

    int getLevel();

    T getData();

    void setData(T data);

    void setUserProp(String key, Object value);

    Object getUserProp(String key);
}
