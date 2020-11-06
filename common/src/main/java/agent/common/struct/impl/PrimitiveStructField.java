package agent.common.struct.impl;

abstract class PrimitiveStructField extends AbstractStructField {
    PrimitiveStructField(byte type, Class<?>... valueClasses) {
        super(type, valueClasses);
    }

    abstract int fixedSize();

    @Override
    int sizeOf(Object value, StructContext context) {
        return fixedSize();
    }
}
