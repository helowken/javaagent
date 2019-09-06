package agent.server.transform.impl.dynamic.rule;

import agent.base.utils.Logger;
import agent.server.transform.impl.dynamic.MethodInfo;

public abstract class TraverseRule<T> {
    private static final Logger logger = Logger.getLogger(TraverseRule.class);
    private ThreadLocal<TraverseItem<T>> local = new ThreadLocal<>();

    protected void methodStart(Object[] args, MethodInfo methodInfo) {
        TraverseItem<T> item = local.get();
        if (item != null && item.tree != null)
            item.tree.destroy();

        if (methodInfo != null) {
            RuleTree<T> tree = new RuleTree<>();
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
            RuleTree<T> tree = item.tree;
            onTreeEnd(tree, tree.getData(), returnValue);
            tree.destroy();
            local.remove();
        }
    }

    protected void methodCallStart(Object[] args, MethodInfo methodInfo) {
        if (methodInfo != null) {
            RuleNode<T> node = new RuleNode<>();
            node.setData(newNodeData(args, methodInfo));
            TraverseItem<T> item = local.get();
            item.currNode.appendChild(node);
            item.currNode = node;
        } else
            logger.error("No methodInfo at method call start.");
    }

    protected void methodCallEnd(Object returnValue) {
        TraverseItem<T> item = local.get();
        if (item != null) {
            RuleNode<T> node = item.currNode;
            onNodeEnd(node, node.getData(), returnValue);
            item.currNode = node.getParent();
        }
    }

    protected abstract T newNodeData(Object[] args, MethodInfo methodInfo);

    protected abstract void onTreeEnd(RuleTree<T> tree, T data, Object returnValue);

    protected abstract void onNodeEnd(RuleNode<T> node, T data, Object returnValue);

    private static class TraverseItem<T> {
        final RuleTree<T> tree;
        RuleNode<T> currNode;

        private TraverseItem(RuleTree<T> tree) {
            this.tree = tree;
        }
    }
}
