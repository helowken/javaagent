package agent.builtin.tools.result.data;

import agent.builtin.tools.result.CostTimeStatItem;

import java.util.Map;
import java.util.TreeMap;

public class InvokeDataConverter {
    public static Map<Integer, Map<String, Object>> serialize(Map<Integer, CostTimeStatItem> data) {
        Map<Integer, Map<String, Object>> rsMap = new TreeMap<>();
        data.forEach(
                (key, value) -> {
                    value.freeze();
                    rsMap.put(
                            key,
                            CostTimeStatItem.CostTimeItemConverter.serialize(value)
                    );
                }
        );
        return rsMap;
    }

    public static Map<Integer, CostTimeStatItem> deserialize(Map<Object, Map<String, Object>> map) {
        Map<Integer, CostTimeStatItem> rsMap = new TreeMap<>();
        map.forEach(
                (key, value) -> rsMap.put(
                        Integer.parseInt(key.toString()),
                        CostTimeStatItem.CostTimeItemConverter.deserialize(value)
                )
        );
        return rsMap;
    }
}
