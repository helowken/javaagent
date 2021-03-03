package agent.base.struct.impl;

import agent.base.struct.BBuff;

import java.util.Map;
import java.util.function.Supplier;

import static agent.base.struct.impl.StructFields.LENGTH_SIZE;

@SuppressWarnings("unchecked")
class MapStructField<T extends Map> extends AbstractStructField {
    private final Supplier<T> newInstanceFunc;

    MapStructField(byte type, Class<T> valueClass, Supplier<T> newInstanceFunc) {
        super(type, valueClass);
        this.newInstanceFunc = newInstanceFunc;
    }

    @Override
    int sizeOf(Object value, StructContext context) {
        Map<Object, Object> map = (Map) value;
        int size = LENGTH_SIZE;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            size += Struct.bytesSize(
                    entry.getKey(),
                    context
            );
            size += Struct.bytesSize(
                    entry.getValue(),
                    context
            );
        }
        return size;
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        Map<Object, Object> map = (Map) value;
        bb.putInt(map.size());
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Struct.serialize(
                    bb,
                    entry.getKey(),
                    context
            );
            Struct.serialize(
                    bb,
                    entry.getValue(),
                    context
            );
        }
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        int size = bb.getInt();
        T map = newInstanceFunc.get();
        for (int i = 0; i < size; ++i) {
            map.put(
                    Struct.deserialize(bb, context),
                    Struct.deserialize(bb, context)
            );
        }
        return map;
    }
}
