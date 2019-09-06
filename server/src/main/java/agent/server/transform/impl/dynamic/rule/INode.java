package agent.server.transform.impl.dynamic.rule;

import java.util.List;

public interface INode<T, N extends INode> {
    N getParent();

    List<N> getChildren();

    boolean isLeaf();

    boolean isRoot();

    void addChildAt(int idx, N child);

    void appendChild(N child);

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

    int getLevel();

    T getData();

    void setData(T data);
}
