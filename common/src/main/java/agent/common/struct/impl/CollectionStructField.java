package agent.common.struct.impl;

import agent.common.struct.BBuff;

import java.util.Collection;

@SuppressWarnings("unchecked")
abstract class CollectionStructField extends CompoundStructField {
    CollectionStructField(Class<?> valueClass) {
        super(valueClass);
    }

    @Override
    public int bytesSize(Object value) {
        int size = Integer.BYTES;
        if (value != null) {
            Collection<Object> coll = (Collection) value;
            for (Object el : coll) {
                size += computeSize(el);
            }
        }
        return size;
    }

    @Override
    public void serialize(BBuff bb, Object value) {
        if (value != null) {
            Collection<Object> coll = (Collection) value;
            bb.putInt(coll.size());
            for (Object el : coll) {
                serializeField(bb, el);
            }
        } else {
            bb.putInt(StructFields.T_NULL);
        }
    }

    @Override
    public Object deserialize(BBuff bb) {
        int len = bb.getInt();
        if (len == StructFields.T_NULL)
            return null;
        Collection<Object> coll = newCollection();
        for (int i = 0; i < len; ++i) {
            coll.add(
                    deserializeField(bb)
            );
        }
        return coll;
    }

    abstract Collection newCollection();
}
