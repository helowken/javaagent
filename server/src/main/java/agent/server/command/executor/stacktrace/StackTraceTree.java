package agent.server.command.executor.stacktrace;

import agent.common.tree.Node;
import agent.common.tree.Tree;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

class StackTraceTree {
    private final Function<String, Integer> getNameId;
    private final Supplier<Integer> dataIdGen;
    private final Tree<StackTraceCountItem> tree = new Tree<>();

    StackTraceTree(Function<String, Integer> getNameId, Supplier<Integer> dataIdGen) {
        this.getNameId = getNameId;
        this.dataIdGen = dataIdGen;
    }

    Tree<StackTraceCountItem> getContent() {
        return tree;
    }

    void combine(List<StackTraceElement> steList) {
        Node<StackTraceCountItem> node = tree;
        for (StackTraceElement ste : steList) {
            node = getOrCreateNode(node, ste);
        }
        node.getData().increase();
    }

    private Node<StackTraceCountItem> getOrCreateNode(Node<StackTraceCountItem> node, StackTraceElement ste) {
        int classId = getNameId.apply(
                ste.getClassName()
        );
        int methodId = getNameId.apply(
                ste.getMethodName()
        );
        Node<StackTraceCountItem> childNode = node.findFirstChild(
                item -> item.getClassId() == classId &&
                        item.getMethodId() == methodId
        );
        return childNode == null ?
                node.appendChild(
                        new Node<>(
                                new StackTraceCountItem(
                                        dataIdGen.get(),
                                        classId,
                                        methodId
                                )
                        )
                ) :
                childNode;
    }
}
