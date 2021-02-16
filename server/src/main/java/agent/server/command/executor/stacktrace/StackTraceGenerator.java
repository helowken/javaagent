package agent.server.command.executor.stacktrace;

import agent.base.utils.Constants;
import agent.common.config.StackTraceConfig;
import agent.common.tree.Tree;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.CompoundFilter;
import agent.server.transform.search.filter.FilterUtils;

import java.util.*;

class StackTraceGenerator {
    private static final String SEP = "#";
    private final Map<String, Integer> nameToId = new HashMap<>();
    private StackTraceTree tree;
    private Map<Long, StackTraceTree> threadIdToTree;
    private Map<String, Set<Long>> threadNameToIds;
    private int nameId = 0;
    private int dataId = 0;
    private final boolean merge;
    private final AgentFilter<String> threadFilter;
    private final AgentFilter<String> stackFilter;
    private final AgentFilter<String> elementFilter;

    StackTraceGenerator(StackTraceConfig config) {
        this.merge = config.isMerge();
        this.threadFilter = newThreadFilter(config);
        this.stackFilter = FilterUtils.newStringFilter(
                config.getStackFilterConfig()
        );
        this.elementFilter = FilterUtils.newStringFilter(
                config.getElementFilterConfig()
        );
        if (merge)
            tree = newTree();
        else {
            threadIdToTree = new HashMap<>();
            threadNameToIds = new HashMap<>();
        }
    }

    private AgentFilter<String> newThreadFilter(StackTraceConfig config) {
        AgentThreadFilter agentThreadFilter = new AgentThreadFilter();
        AgentFilter<String> threadNameFilter = FilterUtils.newStringFilter(
                config.getThreadFilterConfig()
        );
        if (threadNameFilter != null)
            return new CompoundFilter<>(
                    Arrays.asList(
                            agentThreadFilter,
                            threadNameFilter
                    )
            );
        return agentThreadFilter;
    }

    private StackTraceTree newTree() {
        return new StackTraceTree(
                this::getNameId,
                this::getDataId
        );
    }

    StackTraceResult getResult() {
        StackTraceResult rs = new StackTraceResult();
        rs.setMerged(merge);
        rs.setNameToId(nameToId);
        if (tree != null)
            rs.setTree(
                    tree.getContent()
            );
        rs.setThreadNameToIds(threadNameToIds);
        if (threadIdToTree != null) {
            Map<Long, Tree<StackTraceCountItem>> rsMap = new HashMap<>();
            threadIdToTree.forEach(
                    (threadId, tree) -> rsMap.put(
                            threadId,
                            tree.getContent())
            );
            rs.setThreadIdToTree(rsMap);
        }
        return rs;
    }

    void generateStackTraces() {
        Thread.getAllStackTraces().forEach(
                (thread, stackEls) -> {
                    if (threadFilter == null || threadFilter.accept(thread.getName())) {
                        boolean flag = false;
                        String entry;
                        LinkedList<StackTraceElement> steList = new LinkedList<>();
                        for (StackTraceElement ste : stackEls) {
                            entry = ste.getClassName() + SEP + ste.getMethodName();
                            if (elementFilter == null || elementFilter.accept(entry))
                                steList.addFirst(ste);
                            if (!flag && (stackFilter == null || stackFilter.accept(entry)))
                                flag = true;
                        }
                        if (flag && !steList.isEmpty())
                            getTree(thread).combine(steList);
                    }
                }
        );
    }

    private StackTraceTree getTree(Thread thread) {
        if (merge)
            return tree;

        threadNameToIds.computeIfAbsent(
                thread.getName(),
                key -> new HashSet<>()
        ).add(
                thread.getId()
        );
        return threadIdToTree.computeIfAbsent(
                thread.getId(),
                threadId -> newTree()
        );
    }

    private int getNameId(String name) {
        return nameToId.computeIfAbsent(
                name,
                key -> ++nameId
        );
    }

    private int getDataId() {
        return ++dataId;
    }

    private static class AgentThreadFilter implements AgentFilter<String> {
        @Override
        public boolean accept(String v) {
            return v == null || !v.startsWith(Constants.AGENT_THREAD_PREFIX);
        }
    }
}
