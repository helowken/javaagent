package agent.server.command.executor;

import agent.server.transform.search.filter.AgentFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
}
