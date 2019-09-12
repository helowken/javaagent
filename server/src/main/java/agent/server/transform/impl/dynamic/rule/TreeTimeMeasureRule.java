package agent.server.transform.impl.dynamic.rule;

import agent.server.transform.impl.dynamic.MethodInfo;

import java.util.Objects;

public class TreeTimeMeasureRule extends TraverseRule<TreeTimeMeasureRule.TimeData> {

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

    @Override
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
