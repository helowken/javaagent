package agent.base.struct.impl;


import agent.base.struct.BBuff;

import static agent.base.struct.impl.StructFields.T_SHORT;

class ShortStructField extends PrimitiveStructField {
    ShortStructField() {
        super(T_SHORT, Short.class, short.class);
    }

    @Override
    int fixedSize() {
        return Short.BYTES;
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        bb.putShort((Short) value);
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        return bb.getShort();
    }
}
