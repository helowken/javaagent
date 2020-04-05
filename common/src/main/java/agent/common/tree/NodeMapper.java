package agent.common.tree;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class NodeMapper {
    private static final String KEY_DATA = "data";
    private static final String KEY_CHILDREN = "children";

    public static <T> Map<String, Object> serialize(Node<T> node, Function<T, Map<String, Object>> convertFunc) {
        Map<String, Object> rsMap = new HashMap<>();
        Optional.ofNullable(
                node.getData()
        ).ifPresent(
                data -> rsMap.put(
                        KEY_DATA,
                        convertFunc.apply(data)
                )
        );
        List<Map<String, Object>> children = new ArrayList<>();
        node.getChildren().forEach(
                cn -> children.add(
                        serialize(cn, convertFunc)
                )
        );
        rsMap.put(KEY_CHILDREN, children);
        return rsMap;
    }

    public static <T> Node<T> deserialize(Node<T> pn, Map<String, Object> map, Function<Map<String, Object>, T> convertFunc) {
        Node<T> node;
        if (pn == null)
            node = new Tree<>();
        else {
            node = new Node<>();
            pn.appendChild(node);
        }
        Map<String, Object> dataMap = (Map) map.get(KEY_DATA);
        if (dataMap != null)
            node.setData(
                    convertFunc.apply(dataMap)
            );
        Collection<Map<String, Object>> childMaps = (Collection) map.get(KEY_CHILDREN);
        if (childMaps != null) {
            childMaps.forEach(
                    childMap -> deserialize(node, childMap, convertFunc)
            );
        }
        return node;
    }
}
