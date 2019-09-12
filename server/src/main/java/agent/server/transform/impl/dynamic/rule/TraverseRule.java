package agent.server.transform.impl.dynamic.rule;

import agent.base.utils.Logger;
import agent.server.transform.impl.dynamic.MethodInfo;

import java.io.PrintStream;

public abstract class TraverseRule<T> {
    private static final Logger logger = Logger.getLogger(TraverseRule.class);
    private static final String branch = "├─";
    private static final String branch2 = "│   ";
    private static final String end = "└─";
    private static final String indent = "    ";
    private ThreadLocal<TraverseItem<T>> local = new ThreadLocal<>();

    protected void methodStart(Object[] args, MethodInfo methodInfo) {
//        logger.debug("method start: {}", methodInfo);
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
                RuleNode<T> node = new RuleNode<>();
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
            RuleNode<T> node = item.currNode;
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

    protected void printTree(RuleTree<T> tree) {
        printTree(System.out, tree);
    }

    protected void printTree(PrintStream out, RuleTree<T> tree) {
        TreeUtils.printTree(out, tree, this::printNode);
    }

    protected void printNode(PrintStream outputStream, RuleNode<T> node) {
        outputStream.println(createPrefix(node) + nodeToString(node));
    }


    private String createPrefix(RuleNode<T> node) {
        RuleNode<T> parent = node.getParent();
        if (parent == null)
            return "";
        RuleNode<T> tmpNode = parent;
        StringBuilder sb = new StringBuilder();
        while (tmpNode != null) {
            if (tmpNode.getParent() != null) {
                if (tmpNode.getParent().isLastChild(tmpNode))
                    sb.insert(0, indent);
                else
                    sb.insert(0, branch2);
            }
            tmpNode = tmpNode.getParent();
        }
        if (parent.isLastChild(node))
            sb.append(end);
        else
            sb.append(branch);
        return sb.toString();
    }

    protected abstract T newNodeData(Object[] args, MethodInfo methodInfo);

    protected abstract void onTreeEnd(RuleTree<T> tree, T data, Object returnValue);

    protected abstract void onNodeEnd(RuleNode<T> node, T data, Object returnValue);

    protected String nodeToString(RuleNode<T> node) {
        T data = node.getData();
        return data != null ? data.toString() : node.toString();
    }

    private static class TraverseItem<T> {
        final RuleTree<T> tree;
        RuleNode<T> currNode;

        private TraverseItem(RuleTree<T> tree) {
            this.tree = tree;
        }
    }
}
