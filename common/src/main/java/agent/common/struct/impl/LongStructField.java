package agent.common.struct.impl;


import agent.common.struct.BBuff;

import static agent.common.struct.impl.StructFields.T_LONG;

class LongStructField extends PrimitiveStructField {
    LongStructField() {
        super(T_LONG, Long.class, long.class);
    }

    @Override
    int fixedSize() {
        return Long.BYTES;
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        bb.putLong((Long) value);
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        return bb.getLong();
    }
}
