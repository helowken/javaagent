package agent.common.struct.impl;

import agent.base.utils.Pair;
import agent.common.struct.BBuff;
import agent.common.struct.StructField;


abstract class CompoundStructField extends AbstractStructField {
    private static final int TYPE_LENGTH = Byte.BYTES;

    CompoundStructField(Class<?> valueClass) {
        super(valueClass);
    }

    int computeSize(Object value) {
        int size = TYPE_LENGTH;
        if (value != null)
            size += StructFields.detectField(value).bytesSize(value);
        return size;
    }

    void serializeField(BBuff bb, Object value) {
        if (value == null)
            bb.put(StructFields.T_NULL);
        else {
            Pair<Byte, StructField> p = StructFields.detectTypeAndField(value);
            bb.put(p.left);
            p.right.serialize(bb, value);
        }
    }

    Object deserializeField(BBuff bb) {
        byte type = bb.get();
        if (type == StructFields.T_NULL)
            return null;
        return StructFields.getField(type).deserialize(bb);
    }

}
