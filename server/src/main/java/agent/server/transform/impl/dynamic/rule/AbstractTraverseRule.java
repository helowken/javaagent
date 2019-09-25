package agent.server.transform.impl.dynamic.rule;

import agent.base.utils.Logger;
import agent.server.transform.impl.dynamic.MethodInfo;
import agent.server.tree.Node;
import agent.server.tree.Tree;

public abstract class AbstractTraverseRule<T> implements TraverseRule {
    private static final Logger logger = Logger.getLogger(AbstractTraverseRule.class);
    private ThreadLocal<TraverseItem<T>> local = new ThreadLocal<>();

    @Override
    public void methodWrap(Object[] args, Object returnValue, MethodInfo methodInfo, boolean before) {
        if (before)
            methodStart(args, methodInfo);
        else
            methodEnd(returnValue);
    }

    @Override
    public void methodCallWrap(Object[] args, Object returnValue, MethodInfo methodInfo, boolean before) {
        if (before)
            methodCallStart(args, methodInfo);
        else
            methodCallEnd(returnValue);
    }

    protected void methodStart(Object[] args, MethodInfo methodInfo) {
//        logger.debug("method start: {}", methodInfo);
        TraverseItem<T> item = local.get();
        if (item != null && item.tree != null)
            item.tree.destroy();

        if (methodInfo != null) {
            Tree<T> tree = new Tree<>();
            tree.setData(newNodeData(args, methodInfo));
            item = new TraverseItem<>(tree);
            item.currNode = tree;
            local.set(item);
        } else
            logger.error("No methodInfo at method start.");
    }

    protected void methodEnd(Object returnValue) {
        TraverseItem<T> item = local.get();
        if (item != null) {
            Tree<T> tree = item.tree;
            if (tree != null) {
//                logger.debug("method end: {}", tree.getData());
                onTreeEnd(tree, tree.getData(), returnValue);
                tree.destroy();
            } else {
                logger.warn("No tree found.");
            }
            local.remove();
        }
    }

    protected void methodCallStart(Object[] args, MethodInfo methodInfo) {
//        logger.debug("method call start: {}", methodInfo);
        if (methodInfo != null) {
            TraverseItem<T> item = local.get();
            if (item != null) {
                Node<T> node = new Node<>();
                node.setData(newNodeData(args, methodInfo));
                item.currNode.appendChild(node);
                item.currNode = node;
            } else {
                // probably init static fields or member fields
                handleNoneOnMethodCallStart(args, methodInfo);
            }
        } else
            logger.error("No methodInfo at method call start.");
    }

    protected void methodCallEnd(Object returnValue) {
        TraverseItem<T> item = local.get();
        if (item != null) {
            Node<T> node = item.currNode;
//            logger.debug("method call end: {}", node.getData());
            onNodeEnd(node, node.getData(), returnValue);
            item.currNode = node.getParent();
        } else {
            // probably init static fields or member fields
            handleNoneOnMethodCallEnd(returnValue);
        }
    }

    protected void handleNoneOnMethodCallStart(Object[] args, MethodInfo methodInfo) {
    }

    protected void handleNoneOnMethodCallEnd(Object returnValue) {
    }

    protected abstract T newNodeData(Object[] args, MethodInfo methodInfo);

    protected abstract void onTreeEnd(Tree<T> tree, T data, Object returnValue);

    protected abstract void onNodeEnd(Node<T> node, T data, Object returnValue);

    private static class TraverseItem<T> {
        final Tree<T> tree;
        Node<T> currNode;

        private TraverseItem(Tree<T> tree) {
            this.tree = tree;
        }
    }
}
