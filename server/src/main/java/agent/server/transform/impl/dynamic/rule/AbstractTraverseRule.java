package agent.server.transform.impl.dynamic.rule;

import agent.base.utils.Logger;
import agent.server.transform.impl.dynamic.MethodInfo;
import agent.server.tree.Node;
import agent.server.tree.Tree;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractTraverseRule<T> implements TraverseRule {
    private static final Logger logger = Logger.getLogger(AbstractTraverseRule.class);
    private ThreadLocal<TraverseItem<T>> local = new ThreadLocal<>();

    @Override
    public void methodWrap(Object[] args, Object returnValue, MethodInfo methodInfo, boolean before) {
        if (before)
            methodStart(args, methodInfo);
        else
            methodEnd(returnValue, methodInfo);
    }

    @Override
    public void methodCallWrap(Object[] args, Object returnValue, MethodInfo methodInfo, boolean before) {
        if (before)
            methodCallStart(args, methodInfo);
        else
            methodCallEnd(returnValue, methodInfo);
    }

    protected void methodStart(Object[] args, MethodInfo methodInfo) {
        if (methodInfo != null) {
            System.out.println("======= method start: " + methodInfo);
            Tree<T> tree = null;
            TraverseItem<T> item = local.get();
            if (item != null && item.tree != null) {
                if (item.methodInfos.isEmpty())
                    item.tree.destroy();
                else
                    tree = item.tree;
            }
            if (tree == null) {
                tree = new Tree<>();
                tree.setData(
                        newNodeData(args, methodInfo)
                );
                item = new TraverseItem<>(tree);
                item.currNode = tree;
                local.set(item);
            } else
                addNode(item, args, methodInfo);
            item.methodInfos.add(methodInfo.toString());
        } else
            logger.error("No methodInfo at method start.");
    }

    protected void methodEnd(Object returnValue, MethodInfo methodInfo) {
        TraverseItem<T> item = local.get();
        if (item != null) {
            System.out.println("======= method end: " + methodInfo);
            item.methodInfos.remove(methodInfo.toString());
            if (item.methodInfos.isEmpty()) {
                Tree<T> tree = item.tree;
                if (tree != null) {
                    onTreeEnd(tree, tree.getData(), returnValue);
                    tree.destroy();
                } else {
                    logger.warn("No tree found.");
                }
                local.remove();
            } else {
                handleNodeEnd(item, returnValue);
            }
        }
    }

    private void addNode(TraverseItem<T> item, Object[] args, MethodInfo methodInfo) {
        Node<T> node = new Node<>();
        node.setData(
                newNodeData(args, methodInfo)
        );
        item.currNode.appendChild(node);
        item.currNode = node;
    }

    protected void methodCallStart(Object[] args, MethodInfo methodInfo) {
        if (methodInfo != null) {
            System.out.println("======= method call start: " + methodInfo);
            TraverseItem<T> item = local.get();
            if (item != null)
                addNode(item, args, methodInfo);
            else
                // probably init static fields or member fields
                handleNoneOnMethodCallStart(args, methodInfo);
        } else
            logger.error("No methodInfo at method call start.");
    }

    protected void methodCallEnd(Object returnValue, MethodInfo methodInfo) {
        TraverseItem<T> item = local.get();
        if (item != null) {
            System.out.println("======= method call end: " + methodInfo);
            handleNodeEnd(item, returnValue);
        } else
            // probably init static fields or member fields
            handleNoneOnMethodCallEnd(returnValue);
    }

    private void handleNodeEnd(TraverseItem<T> item, Object returnValue) {
        Node<T> node = item.currNode;
        onNodeEnd(node, node.getData(), returnValue);
        item.currNode = node.getParent();
    }

    protected void handleNoneOnMethodCallStart(Object[] args, MethodInfo methodInfo) {
    }

    protected void handleNoneOnMethodCallEnd(Object returnValue) {
    }

    protected abstract T newNodeData(Object[] args, MethodInfo methodInfo);

    protected abstract void onTreeEnd(Tree<T> tree, T data, Object returnValue);

    protected abstract void onNodeEnd(Node<T> node, T data, Object returnValue);

    private static class TraverseItem<T> {
        final Set<String> methodInfos = new HashSet<>();
        final Tree<T> tree;
        Node<T> currNode;

        private TraverseItem(Tree<T> tree) {
            this.tree = tree;
        }
    }
}
