package agent.common.struct.impl;

import agent.common.struct.BBuff;
import agent.common.struct.StructField;

import static agent.common.struct.impl.StructFields.TYPE_SIZE;
import static agent.common.struct.impl.StructFields.T_NULL;

class NullStructField implements StructField {

    @Override
    public byte getType() {
        return T_NULL;
    }

    @Override
    public boolean matchType(Object value) {
        return value == null;
    }

    @Override
    public Class<?>[] getValueClasses() {
        return new Class[0];
    }

    @Override
    public int bytesSize(Object value, StructContext context) {
        return TYPE_SIZE;
    }

    @Override
    public void serialize(BBuff bb, Object value, StructContext context) {
        bb.put(T_NULL);
    }

    @Override
    public Object deserialize(BBuff bb, StructContext context) {
        return null;
    }
}
