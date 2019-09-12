package agent.server.transform.impl.dynamic.rule;

import agent.base.utils.IndentUtils;
import agent.server.transform.impl.dynamic.MethodInfo;

import java.io.PrintStream;
import java.util.Objects;

public class TreeTimeMeasureRule extends TraverseRule<TreeTimeMeasureRule.TimeData> {
    private static final String branch = "├─";
    private static final String branch2 = "│   ";
    private static final String end = "└─";

    @Override
    protected TimeData newNodeData(Object[] args, MethodInfo methodInfo) {
        return new TimeData(methodInfo.className, methodInfo.methodName + methodInfo.signature, System.currentTimeMillis());
    }

    @Override
    protected void onTreeEnd(RuleTree<TimeData> tree, TimeData data, Object returnValue) {
        data.endTime = System.currentTimeMillis();
        printTree(tree);
    }

    @Override
    protected void onNodeEnd(RuleNode<TimeData> node, TimeData data, Object returnValue) {
        data.endTime = System.currentTimeMillis();
    }

    protected void printTree(RuleTree<TimeData> tree) {
        printTree(System.out, tree);
    }

    protected void printTree(PrintStream out, RuleTree<TimeData> tree) {
        TreeUtils.printTree(out, tree, this::printNode);
    }

    protected void printNode(PrintStream outputStream, RuleNode<TimeData> node) {
        outputStream.println(createPrefix(node) + nodeToString(node));
    }

    private String createPrefix(RuleNode<TimeData> node) {
        RuleNode<TimeData> parent = node.getParent();
        if (parent == null)
            return "";
        final String indent = IndentUtils.getIndent();
        RuleNode<TimeData> tmpNode = parent;
        StringBuilder sb = new StringBuilder();
        while (tmpNode != null) {
            if (tmpNode.getParent() != null) {
                if (tmpNode.getParent().isLastChild(node))
                    sb.insert(0, indent);
                else
                    sb.insert(0, indent);
//                    sb.insert(0, branch2);
            }
            tmpNode = tmpNode.getParent();
        }
        if (parent.isLastChild(node))
            sb.append(end);
        else
            sb.append(branch);
        return sb.toString();
    }

    protected String nodeToString(RuleNode<TimeData> node) {
        RuleNode<TimeData> parent = node.getParent();
        TimeData nodeData = node.getData();
        if (parent != null) {
            TimeData parentData = parent.getData();
            if (Objects.equals(parentData.className, nodeData.className))
                return nodeData.getString(true);
        }
        return nodeData.getString(false);
    }

    public static class TimeData {
        public final String className;
        public final String methodDesc;
        public final long startTime;
        public long endTime;

        TimeData(String className, String methodDesc, long startTime) {
            this.className = className;
            this.methodDesc = methodDesc;
            this.startTime = startTime;
        }

        public String getString(boolean isShort) {
            String s = isShort ? methodDesc : "<" + className + "> " + methodDesc;
            return s + ": " + (endTime - startTime) + "ms";
        }
    }
}
