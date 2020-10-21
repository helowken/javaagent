package agent.common.struct.impl;

import agent.common.struct.BBuff;
import agent.common.struct.StructField;

import java.util.Collection;

public class PojoStructField extends AbstractStructField {
    private static final StructField field = StructFields.newList();

    PojoStructField() {
        super(Object.class);
    }

    @Override
    public boolean match(Class<?> clazz) {
        return Object.class.isAssignableFrom(clazz);
    }

    @Override
    public int bytesSize(Object value) {
        return field.bytesSize(
                value == null ? null : PojoStructCache.getPojoValues(value)
        );
    }

    @Override
    public void serialize(BBuff bb, Object value) {
        field.serialize(
                bb,
                value == null ? null : PojoStructCache.getPojoValues(value)
        );
    }

    @Override
    public Object deserialize(BBuff bb) {
        return new PojoStructCache.PojoValues(
                (Collection) field.deserialize(bb)
        );
    }
}
