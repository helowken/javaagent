package agent.common.struct.impl;

import agent.base.utils.Pair;
import agent.common.struct.BBuff;
import agent.common.struct.StructField;

import static agent.common.struct.impl.StructFields.TYPE_SIZE;


abstract class CompoundStructField extends AbstractStructField {

    CompoundStructField(Class<?> valueClass) {
        super(valueClass);
    }

    int computeSize(Object value) {
        int size = TYPE_SIZE;
        if (value != null)
            size += StructFields.detectField(value).bytesSize(value);
        return size;
    }

    void serializeField(BBuff bb, Object value) {
        if (value == null)
            bb.put(StructFields.NULL);
        else {
            Pair<Byte, StructField> p = StructFields.detectTypeAndField(value);
            bb.put(p.left);
            p.right.serialize(bb, value);
        }
    }

    Object deserializeField(BBuff bb) {
        byte type = bb.get();
        if (type == StructFields.NULL)
            return null;
        return StructFields.getField(type).deserialize(bb);
    }

}
