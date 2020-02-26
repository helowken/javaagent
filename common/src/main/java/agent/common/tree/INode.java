package agent.common.tree;

import java.util.List;
import java.util.function.Predicate;

public interface INode<T, N extends INode> {
    N getParent();

    List<N> getChildren();

    List<N> findChildren(Predicate<T> predicate);

    N findFirstChild(Predicate<T> predicate);

    boolean isLeaf();

    boolean isRoot();

    void reverseChildren();

    N addChildAt(int idx, N child);

    N appendChild(N child);

    void removeChild(N child);

    void removeChildAt(int idx);

    void removeAll(boolean destroy);

    N lastChild();

    N firstChild();

    N getChildAt(int idx);

    int indexOf(N child);

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
}
