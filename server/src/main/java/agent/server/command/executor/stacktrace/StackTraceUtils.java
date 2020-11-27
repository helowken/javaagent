package agent.server.command.executor.stacktrace;

import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.server.transform.search.filter.AgentFilter;

import java.util.*;
import java.util.function.Function;

public class StackTraceUtils {
    public static <K, V> Map<K, List<V>> getStackTraces(Map<K, List<V>> stMap, Function<K, String> keyFunc, Function<V, String> valueFunc,
                                                        AgentFilter<String> threadFilter, AgentFilter<String> elementFilter, AgentFilter<String> stackFilter) {
        Map<K, List<V>> rsMap = new HashMap<>();
        stMap.forEach(
                (key, values) -> {
                    String name = keyFunc.apply(key);
                    if ((threadFilter == null || threadFilter.accept(name))) {
                        List<V> rsList = new ArrayList<>(values.size());
                        boolean flag = false;
                        String entry;
                        for (V value : values) {
                            entry = valueFunc.apply(value);
                            if (elementFilter == null || elementFilter.accept(entry))
                                rsList.add(value);
                            if (!flag && (stackFilter == null || stackFilter.accept(entry)))
                                flag = true;
                        }
                        if (flag && !rsList.isEmpty())
                            rsMap.put(key, rsList);
                    }
                }
        );
        return rsMap;
    }

    private static Node<StackTraceCountItem> getOrCreateNode(Node<StackTraceCountItem> node, int classId, int methodId) {
        Node<StackTraceCountItem> childNode = node.findFirstChild(
                item -> item.getClassId() == classId &&
                        item.getMethodId() == methodId
        );
        if (childNode == null)
            childNode = node.appendChild(
                    new Node<>(
                            new StackTraceCountItem(classId, methodId)
                    )
            );
        return childNode;
    }

    public static <K, V> void convertStackTraceToTree(Tree<StackTraceCountItem> tree, Map<K, List<V>> stMap, boolean merge,
                                                      Function<K, Integer> keyClassIdFunc, Function<V, Integer> valueClassIdFunc, Function<V, Integer> valueMethodIdFunc) {
        stMap.forEach(
                (key, values) -> {
                    if (!values.isEmpty()) {
                        List<V> els = new ArrayList<>(values);
                        Collections.reverse(els);

                        int classId;
                        int methodId;
                        if (merge) {
                            V el = els.get(0);
                            els.remove(0);
                            classId = valueClassIdFunc.apply(el);
                            methodId = valueMethodIdFunc.apply(el);
                        } else {
                            classId = keyClassIdFunc.apply(key);
                            methodId = 0;
                        }

                        Node<StackTraceCountItem> node = getOrCreateNode(tree, classId, methodId);
                        if (els.isEmpty())
                            node.getData().increase();
                        else {
                            for (int i = 0, len = els.size(); i < len; ++i) {
                                V el = els.get(i);
                                classId = valueClassIdFunc.apply(el);
                                methodId = valueMethodIdFunc.apply(el);
                                node = getOrCreateNode(node, classId, methodId);
                                if (i == len - 1)
                                    node.getData().increase();
                            }
                        }
                    }
                }
        );
    }
}
