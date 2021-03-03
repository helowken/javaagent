package agent.base.struct.impl;

import agent.base.struct.BBuff;

import static agent.base.struct.impl.StructFields.T_LIST;
import static agent.base.struct.impl.StructFields.T_POJO;

@SuppressWarnings("unchecked")
class PojoStructField extends AbstractStructField {
    private static final int POJO_TYPE_SIZE = Integer.BYTES;

    PojoStructField() {
        super(T_POJO, Object.class);
    }

    @Override
    int sizeOf(Object value, StructContext context) {
        return POJO_TYPE_SIZE + StructFields.getField(T_LIST)
                .bytesSize(
                        context.getPojoValues(value),
                        context
                );
    }

    @Override
    void serializeObject(BBuff bb, Object value, StructContext context) {
        PojoValues pojoValues = context.getPojoValues(value);
        bb.putInt(pojoValues.type);
        StructFields.getField(T_LIST).serialize(bb, pojoValues, context);
    }

    @Override
    Object deserializeObject(BBuff bb, StructContext context) {
        int type = bb.getInt();
        PojoValues pojoValues = new PojoValues(
                type,
                Struct.deserialize(bb, context)
        );
        return context.createPojo(pojoValues);
    }
}
