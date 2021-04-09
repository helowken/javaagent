package agent.base.struct.impl;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.TypeObject;
import agent.base.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
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

    public static <V> PojoFieldProperty<V> create(Class<V> clazz, String fieldName, int index) {
        return create(clazz, fieldName, fieldName, index);
    }

    public static <V> PojoFieldProperty<V> create(Class<V> clazz, String fieldName, String propertyName, int index) {
        return Utils.wrapToRtError(
                () -> create(
                        ReflectionUtils.getField(clazz, fieldName),
                        propertyName,
                        index
                )
        );
    }

    public static <V> PojoFieldProperty<V> create(Field field, int index) {
        return create(
                field,
                field.getName(),
                index
        );
    }

    public static <V> PojoFieldProperty<V> create(Field field, String propertyName, int index) {
        Class<V> clazz = (Class<V>) field.getDeclaringClass();
        return new PojoFieldProperty(
                field.getGenericType(),
                index,
                getFieldValueSetter(clazz, propertyName, field),
                getFieldValueGetter(clazz, propertyName, field)
        );
    }

    private static <T> PojoFieldValueSetter<T, Object> getFieldValueSetter(Class<?> clazz, String propertyName, Field field) {
        Method method = getMethod(clazz, propertyName, field.getType());
        return method != null ? method::invoke : field::set;
    }

    private static <T> PojoFieldValueGetter<T, Object> getFieldValueGetter(Class<?> clazz, String propertyName, Field field) {
        Method method = getMethod(clazz, propertyName, null);
        return method != null ? method::invoke : field::get;
    }

    private static Method getMethod(Class<?> clazz, String propertyName, Class<?> fieldClass) {
        String name = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        String[] methodNames;
        Class[] argClasses;
        if (fieldClass == null) {
            methodNames = new String[]{
                    "get" + name,
                    "is" + name
            };
            argClasses = new Class[0];
        } else {
            methodNames = new String[]{
                    "set" + name
            };
            argClasses = new Class[]{fieldClass};
        }
        for (String methodName : methodNames) {
            try {
                return clazz.getMethod(methodName, argClasses);
            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
            }
        }
        return null;
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
