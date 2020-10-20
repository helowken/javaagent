package agent.common.struct.impl;

abstract class PremitiveStructField extends AbstractStructField {
    PremitiveStructField(Class<?> valueClass) {
        super(valueClass);
    }

    abstract Class<?> getPrimitiveClass();

    @Override
    public boolean match(Class<?> clazz) {
        return getPrimitiveClass() == clazz || super.match(clazz);
    }
}
