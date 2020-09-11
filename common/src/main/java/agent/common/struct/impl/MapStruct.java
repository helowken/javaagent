package agent.common.struct.impl;

import agent.common.struct.BBuff;
import agent.common.struct.Struct;
import agent.common.struct.StructField;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MapStruct<K, V> implements Struct {
    private StructField field;
    private final Map<K, V> valueMap;

    protected MapStruct() {
        this(
                new HashMap<>(),
                StructFields.newMap()
        );
    }

    public MapStruct(Map<K, V> valueMap, MapStructField field) {
        this.valueMap = valueMap;
        this.field = field;
    }

    public void put(K key, V value) {
        valueMap.put(key, value);
    }

    public V get(K key) {
        return valueMap.get(key);
    }

    public void clear() {
        valueMap.clear();
    }

    public void remove(K key) {
        valueMap.remove(key);
    }

    public int size() {
        return valueMap.size();
    }

    public void putAll(Map<K, V> map) {
        valueMap.putAll(map);
    }

    public Map<K, V> getAll() {
        return Collections.unmodifiableMap(valueMap);
    }

    public int bytesSize() {
        return field.bytesSize(valueMap);
    }

    @Override
    public void deserialize(BBuff bb) {
        valueMap.putAll((Map) field.deserialize(bb));
    }

    @Override
    public void serialize(BBuff bb) {
        field.serialize(bb, valueMap);
    }
}
