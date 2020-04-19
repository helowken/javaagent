package agent.builtin.tools.result.data;

import agent.builtin.tools.result.CostTimeStatItem;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class CallChainDataConverter {
    private static final String KEY_ID = "id";
    private static final String KEY_INVOKE_ID = "invokeId";
    private static final String KEY_ITEM = "item";

    public static Map<String, Object> serialize(CallChainData data) {
        data.item.freeze();
        Map<String, Object> rsMap = new HashMap<>();
        rsMap.put(KEY_ID, data.id);
        rsMap.put(KEY_INVOKE_ID, data.invokeId);
        rsMap.put(
                KEY_ITEM,
                CostTimeStatItem.CostTimeItemConverter.serialize(data.item)
        );
        return rsMap;
    }

    public static CallChainData deserialize(Map<String, Object> map) {
        return new CallChainData(
                Integer.parseInt(map.get(KEY_ID).toString()),
                Integer.parseInt(map.get(KEY_INVOKE_ID).toString()),
                CostTimeStatItem.CostTimeItemConverter.deserialize(
                        (Map) map.get(KEY_ITEM)
                )
        );
    }
}
