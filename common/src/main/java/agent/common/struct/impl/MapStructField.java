package agent.common.struct.impl;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
class MapStructField<T extends Map> extends CompoundStructField {
    private final Supplier<T> newInstanceFunc;

    MapStructField(Class<T> valueClass, Supplier<T> newInstanceFunc) {
        super(valueClass);
        this.newInstanceFunc = newInstanceFunc;
    }

    @Override
    public int bytesSize(Object value) {
        Map map = (Map) value;
        int size = Integer.BYTES;
        for (Object key : map.keySet()) {
            size += computeSize(key);
            size += computeSize(map.get(key));
        }
        return size;
    }

    @Override
    public void serialize(ByteBuffer bb, Object value) {
        Map map = (Map) value;
        bb.putInt(map.size());
        for (Object key : map.keySet()) {
            serializeField(bb, key);
            serializeField(bb, map.get(key));
        }
    }

    @Override
    public Object deserialize(ByteBuffer bb) {
        int size = bb.getInt();
        Map map = newInstanceFunc.get();
        for (int i = 0; i < size; ++i) {
            Object key = deserializeField(bb);
            Object value = deserializeField(bb);
            map.put(key, value);
        }
        return map;
    }
}
