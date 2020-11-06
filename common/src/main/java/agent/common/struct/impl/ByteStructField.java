package agent.common.struct.impl;


import agent.common.struct.BBuff;

import static agent.common.struct.impl.StructFields.T_BYTE;

class ByteStructField extends PrimitiveStructField {
    ByteStructField() {
        super(T_BYTE, Byte.class, byte.class);
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        bb.put((Byte) value);
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        return bb.get();
    }

    @Override
    int fixedSize() {
        return Byte.BYTES;
    }
}
