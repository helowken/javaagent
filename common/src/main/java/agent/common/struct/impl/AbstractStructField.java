package agent.common.struct.impl;

import agent.common.struct.StructField;

abstract class AbstractStructField implements StructField {
    private final Class<?> valueClass;

    AbstractStructField(Class<?> valueClass) {
        this.valueClass = valueClass;
    }

    @Override
    public boolean matchType(Object value) {
        return valueClass.isInstance(value);
    }

    @Override
    public Class<?> getValueClass() {
        return valueClass;
    }
}
