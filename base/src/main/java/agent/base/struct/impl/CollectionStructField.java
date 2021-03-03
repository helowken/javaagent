package agent.base.struct.impl;

import agent.base.struct.BBuff;

import java.util.Collection;
import java.util.function.Supplier;

import static agent.base.struct.impl.StructFields.LENGTH_SIZE;

@SuppressWarnings("unchecked")
class CollectionStructField<T extends Collection> extends AbstractStructField {
    private final Supplier<T> newInstanceFunc;

    CollectionStructField(byte type, Class<?> valueClass, Supplier<T> newInstanceFunc) {
        super(type, valueClass);
        this.newInstanceFunc = newInstanceFunc;
    }

    @Override
    int sizeOf(Object value, StructContext context) {
        int size = LENGTH_SIZE;
        Collection<Object> coll = (Collection) value;
        for (Object el : coll) {
            size += Struct.bytesSize(el, context);
        }
        return size;
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        Collection<Object> coll = (Collection) value;
        bb.putInt(coll.size());
        for (Object el : coll) {
            Struct.serialize(bb, el, context);
        }
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        int len = bb.getInt();
        T coll = newInstanceFunc.get();
        for (int i = 0; i < len; ++i) {
            coll.add(
                    Struct.deserialize(bb, context)
            );
        }
        return coll;
    }
}
