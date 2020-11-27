package agent.common.tree;

import agent.common.struct.impl.annotation.PojoClass;

import static agent.common.tree.Tree.POJO_TYPE;

@PojoClass(type = POJO_TYPE)
public class Tree<T> extends Node<T> {
    public static final int POJO_TYPE = 1001;

    public Tree() {
    }

    public Tree(T data) {
        super(data);
    }
}
