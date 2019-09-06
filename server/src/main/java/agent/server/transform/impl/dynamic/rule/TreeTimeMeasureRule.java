package agent.server.transform.impl.dynamic.rule;

import agent.base.utils.IndentUtils;
import agent.server.transform.impl.dynamic.MethodInfo;

public class TreeTimeMeasureRule extends TraverseRule<TreeTimeMeasureRule.TimeData> {

    @Override
    protected TimeData newNodeData(Object[] args, MethodInfo methodInfo) {
        String key = " <" + methodInfo.className + "> " + methodInfo.methodName + methodInfo.signature;
        return new TimeData(key, System.currentTimeMillis());
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

    private void printTree(RuleTree<TimeData> tree) {
        TreeUtils.printTree(tree,
                (outputStream, node) -> {
                    int level = node.getLevel();
                    String prefix = "";
                    if (level > 0) {
                        prefix += IndentUtils.getIndent(level);
                        if (node.getParent().isLastChild(node))
                            prefix += "└─";
                        else
                            prefix += "├─";
                    }
                    outputStream.println(prefix + node.getData());
                }
        );
    }

    static class TimeData {
        final String key;
        final long startTime;
        long endTime;

        TimeData(String key, long startTime) {
            this.key = key;
            this.startTime = startTime;
        }

        public String toString() {
            return key + ": " + (endTime - startTime) + "ms";
        }
    }
}