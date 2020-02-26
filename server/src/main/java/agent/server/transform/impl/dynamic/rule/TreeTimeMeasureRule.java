package agent.server.transform.impl.dynamic.rule;

import agent.server.transform.impl.dynamic.MethodInfo;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;

import java.util.Objects;

public class TreeTimeMeasureRule extends AbstractTraverseRule<TreeTimeMeasureRule.TimeData> {

    @Override
    protected TimeData newNodeData(Object[] args, MethodInfo methodInfo) {
        return new TimeData(methodInfo.className, methodInfo.methodName + methodInfo.signature, System.currentTimeMillis());
    }

    @Override
    protected void onTreeEnd(Tree<TimeData> tree, TimeData data, Object returnValue) {
        data.endTime = System.currentTimeMillis();
        TreeUtils.printTree(System.out, tree, null, (node, config) -> nodeToString(node));
    }

    @Override
    protected void onNodeEnd(Node<TimeData> node, TimeData data, Object returnValue) {
        data.endTime = System.currentTimeMillis();
    }

    protected String nodeToString(Node<TimeData> node) {
        Node<TimeData> parent = node.getParent();
        TimeData nodeData = node.getData();
        if (parent != null) {
            TimeData parentData = parent.getData();
            if (Objects.equals(parentData.className, nodeData.className))
                return nodeData.getString(true);
        }
        return nodeData.getString(false);
    }

    public static class TimeData {
        final String className;
        final String methodDesc;
        final long startTime;
        long endTime;

        TimeData(String className, String methodDesc, long startTime) {
            this.className = className;
            this.methodDesc = methodDesc;
            this.startTime = startTime;
        }

        String getString(boolean isShort) {
            String s = isShort ? methodDesc : "<" + className + "> " + methodDesc;
            return s + ": " + (endTime - startTime) + "ms";
        }

        @Override
        public String toString() {
            return getString(false);
        }
    }
}
