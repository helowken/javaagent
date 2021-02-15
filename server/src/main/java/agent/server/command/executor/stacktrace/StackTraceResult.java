package agent.server.command.executor.stacktrace;


import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;
import agent.common.tree.Tree;

import java.util.Map;
import java.util.Set;

import static agent.server.command.executor.stacktrace.StackTraceResult.POJO_TYPE;

@PojoClass(type = POJO_TYPE)
public class StackTraceResult {
    public static final int POJO_TYPE = 1;
    @PojoProperty(index = 1)
    private boolean merge;
    @PojoProperty(index = 2)
    private Tree<StackTraceCountItem> tree;
    @PojoProperty(index = 3)
    private Map<Long, Tree<StackTraceCountItem>> threadIdToTree;
    @PojoProperty(index = 4)
    private Map<String, Integer> nameToId;
    @PojoProperty(index = 5)
    private Map<String, Set<Long>> threadNameToIds;

    public boolean isMerge() {
        return merge;
    }

    public void setMerge(boolean merge) {
        this.merge = merge;
    }

    public Tree<StackTraceCountItem> getTree() {
        return tree;
    }

    public void setTree(Tree<StackTraceCountItem> tree) {
        this.tree = tree;
    }

    public Map<Long, Tree<StackTraceCountItem>> getThreadIdToTree() {
        return threadIdToTree;
    }

    public void setThreadIdToTree(Map<Long, Tree<StackTraceCountItem>> threadIdToTree) {
        this.threadIdToTree = threadIdToTree;
    }

    public Map<String, Integer> getNameToId() {
        return nameToId;
    }

    public void setNameToId(Map<String, Integer> nameToId) {
        this.nameToId = nameToId;
    }

    public Map<String, Set<Long>> getThreadNameToIds() {
        return threadNameToIds;
    }

    public void setThreadNameToIds(Map<String, Set<Long>> threadNameToIds) {
        this.threadNameToIds = threadNameToIds;
    }
}
