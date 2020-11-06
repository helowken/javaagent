package agent.common.struct.impl;

import agent.base.utils.TypeObject;

import java.lang.reflect.Type;

public class PojoFieldProperty<T> {
    final Type type;
    final int index;
    final PojoFieldValueSetter setter;
    final PojoFieldValueGetter getter;

    public PojoFieldProperty(Type type, int index, PojoFieldValueSetter<T, Object> setter, PojoFieldValueGetter<T, Object> getter) {
        this.type = type;
        this.index = index;
        this.setter = setter;
        this.getter = getter;
    }

    public PojoFieldProperty(TypeObject typeObj, int index, PojoFieldValueSetter<T, Object> setter, PojoFieldValueGetter<T, Object> getter) {
        this(typeObj.type, index, setter, getter);
    }

    @Override
    public String toString() {
        return "FieldProperty{" +
                "type=" + type +
                ", index=" + index +
                ", setter=" + setter +
                ", getter=" + getter +
                '}';
    }
}
