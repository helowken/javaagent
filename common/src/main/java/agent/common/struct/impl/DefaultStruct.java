package agent.common.struct.impl;

import agent.common.struct.BBuff;
import agent.common.struct.Struct;
import agent.common.struct.StructField;


public class DefaultStruct implements Struct {
    private StructField field;
    private Object value;

    DefaultStruct(StructField field) {
        this.field = field;
    }

    public void set(Object v) {
        if (v != null && !field.matchType(v))
            throw new RuntimeException("Value type mismatch, value: " + v);
        this.value = v;
    }

    @SuppressWarnings("unchecked")
    public <V> V get() {
        return (V) this.value;
    }

    @Override
    public void deserialize(BBuff bb) {
        set(field.deserialize(bb));
    }

    @Override
    public void serialize(BBuff bb) {
        field.serialize(bb, value);
    }

    @Override
    public int bytesSize() {
        return field.bytesSize(value);
    }
}
