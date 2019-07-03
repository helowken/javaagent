package agent.common.struct.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
class MapStructField extends CompoundStructField {
    MapStructField() {
        super(Map.class);
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
        Map map = new HashMap();
        for (int i = 0; i < size; ++i) {
            Object key = deserializeField(bb);
            Object value = deserializeField(bb);
            map.put(key, value);
        }
        return map;
    }
}
