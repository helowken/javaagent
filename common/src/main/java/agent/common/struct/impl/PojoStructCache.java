package agent.common.struct.impl;

import agent.base.utils.Utils;
import agent.common.utils.annotation.PojoProperty;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class PojoStructCache {
    private static final Map<Class<?>, List<FieldProperty>> classToFieldPropertyList = new ConcurrentHashMap<>();
    private static final Map<Class<?>, FieldTypeConverter> classToFieldTypeConverter = new ConcurrentHashMap<>();
    private static final Comparator<FieldProperty> comparator = (o1, o2) -> {
        if (o1.index > o2.index)
            return 1;
        else if (o1.index < o2.index)
            return -1;
        return 0;
    };

    public static void clear() {
        classToFieldPropertyList.clear();
    }

    public static <T> void setFieldTypeConverter(Class<T> pojoClass, FieldTypeConverter<T> fieldTypeConverter) {
        classToFieldTypeConverter.put(pojoClass, fieldTypeConverter);
    }

    static void setPojoValues(Object pojo, List<Object> fieldValues) {
        List<FieldProperty> fieldPropertyList = getTotalFieldPropertyList(pojo.getClass());
        if (fieldValues.size() != fieldPropertyList.size())
            throw new RuntimeException("Field values size is: " + fieldValues.size() +
                    ", but field list size is: " + fieldPropertyList.size() +
                    ", Pojo to be populated: " + pojo);
        Utils.wrapToRtError(
                () -> {
                    FieldProperty fieldProperty;
                    Object fieldValue;
                    for (int i = 0, len = fieldPropertyList.size(); i < len; ++i) {
                        fieldProperty = fieldPropertyList.get(i);
                        fieldValue = convertValue(
                                pojo,
                                fieldProperty.index,
                                fieldProperty.type,
                                fieldValues.get(i),
                                0,
                                false
                        );
                        fieldProperty.setter.invoke(pojo, fieldValue);
                    }
                }
        );
    }

    private static Object convertValue(Object pojo, int fieldIndex, Type currType, Object originalValue, int level, boolean isKey) {
        if (originalValue == null)
            return null;

        Type[] argTypes = null;
        Class<?> currClass = null;
        if (currType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) currType;
            currClass = (Class<?>) paramType.getRawType();
            argTypes = paramType.getActualTypeArguments();
        } else if (currType instanceof Class) {
            currClass = (Class<?>) currType;
        }

        if (currClass != null) {
            if (currClass.isArray()) {
                int len = Array.getLength(originalValue);
                Class<?> elClass = currClass.getComponentType();
                Object array = Array.newInstance(elClass, len);
                Object el;
                for (int i = 0; i < len; ++i) {
                    el = Array.get(originalValue, i);
                    Array.set(
                            array,
                            i,
                            convertValue(pojo, fieldIndex, elClass, el, level + 1, false)
                    );
                }
                return array;
            } else if (Map.class.isAssignableFrom(currClass)) {
                Type keyType = null;
                Type valueType = null;
                if (argTypes != null && argTypes.length >= 2) {
                    keyType = argTypes[0];
                    valueType = argTypes[1];
                }

                Map<Object, Object> rsMap = (Map) newInstanceForType(currClass, 0);
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) originalValue).entrySet()) {
                    rsMap.put(
                            convertValue(pojo, fieldIndex, keyType, entry.getKey(), level + 1, true),
                            convertValue(pojo, fieldIndex, valueType, entry.getValue(), level + 1, false)
                    );
                }
                return rsMap;
            } else if (Collection.class.isAssignableFrom(currClass)) {
                Type valueType = null;
                if (argTypes != null && argTypes.length >= 1)
                    valueType = argTypes[0];
                Collection<Object> rsColl = (Collection) newInstanceForType(currClass, 0);
                for (Object value : ((Collection) originalValue)) {
                    rsColl.add(
                            convertValue(pojo, fieldIndex, valueType, value, level + 1, false)
                    );
                }
                return rsColl;
            }
        }

        if (isPojoValues(originalValue)) {
            PojoValues pojoValues = (PojoValues) originalValue;
            Object pojoOfField = newInstanceForType(
                    convertPojoFieldType(pojo, fieldIndex, currClass, level, isKey),
                    pojoValues.size()
            );
            setPojoValues(pojoOfField, pojoValues);
            return pojoOfField;
        }
        return originalValue;
    }

    private static Class<?> convertPojoFieldType(Object pojo, int fieldIndex, Type currType, int level, boolean isKey) {
        FieldTypeConverter converter = classToFieldTypeConverter.get(pojo.getClass());
        Type rsType = null;
        if (converter != null)
            rsType = converter.convert(pojo, currType, fieldIndex, level, isKey);
        else if (currType instanceof Class)
            rsType = currType;

        if (rsType instanceof Class)
            return (Class<?>) rsType;

        throw new RuntimeException("No Class found! FieldIndex: " + fieldIndex +
                ", currType: " + currType +
                ", level: " + level +
                ", isKey: " + isKey +
                ", pojo: " + pojo
        );
    }

    static Collection<Object> getPojoValues(Object o) {
        if (o == null)
            throw new IllegalArgumentException();
        return getTotalFieldPropertyList(o.getClass())
                .stream()
                .map(
                        fieldProperty -> Utils.wrapToRtError(
                                () -> fieldProperty.getter.invoke(o)
                        )
                )
                .collect(
                        Collectors.toList()
                );
    }

    private static boolean isPojoValues(Object value) {
        return value instanceof PojoValues;
    }

    private static Object newInstanceForType(Class<?> clazz, int size) {
        if (clazz.isArray())
            return Array.newInstance(
                    clazz.getComponentType(),
                    size
            );
        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
            }
        }
        if (Map.class.isAssignableFrom(clazz))
            return new HashMap<>();
        if (Set.class.isAssignableFrom(clazz))
            return new HashSet<>();
        if (Collection.class.isAssignableFrom(clazz))
            return new ArrayList<>();
        throw new RuntimeException("Can't new instance for class: " + clazz);
    }

    private static List<FieldProperty> getTotalFieldPropertyList(Class<?> pojoClass) {
        return classToFieldPropertyList.computeIfAbsent(
                pojoClass,
                clazz -> {
                    Set<String> nameSet = new HashSet<>();
                    List<FieldProperty> fieldPropertyList = new ArrayList<>();
                    while (clazz != null) {
                        fieldPropertyList.addAll(
                                getFieldPropertyList(clazz, nameSet)
                        );
                        clazz = clazz.getSuperclass();
                    }
                    return fieldPropertyList;
                }
        );
    }

    private static List<FieldProperty> getFieldPropertyList(Class<?> clazz, Set<String> nameSet) {
        List<FieldProperty> fieldPropertyList = new ArrayList<>();
        Set<Integer> indexes = new TreeSet<>();
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            String propertyName;
            int index;
            for (Field field : fields) {
                PojoProperty structProperty = field.getAnnotation(PojoProperty.class);
                if (structProperty != null) {
                    index = structProperty.index();
                    if (indexes.contains(index))
                        throw new RuntimeException("Duplicated index: " + index + ", field: " + field + ", class: " + clazz);
                    indexes.add(index);

                    propertyName = structProperty.name();
                    if (Utils.isBlank(propertyName))
                        propertyName = field.getName();
                    if (nameSet.contains(propertyName))
                        continue;
                    nameSet.add(propertyName);

                    fieldPropertyList.add(
                            new FieldProperty(
                                    field.getGenericType(),
                                    index,
                                    getMethod(clazz, propertyName, field.getType()),
                                    getMethod(clazz, propertyName, null)
                            )
                    );
                }
            }
            fieldPropertyList.sort(comparator);
        }
        return fieldPropertyList;
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
            }
        }
        throw new RuntimeException(
                "No method found for property: " + propertyName +
                        " for " + (fieldClass == null ? "getting" : "setting")
        );
    }

    private static class FieldProperty {
        final Type type;
        final int index;
        final Method setter;
        final Method getter;

        private FieldProperty(Type type, int index, Method setter, Method getter) {
            this.type = type;
            this.index = index;
            this.setter = setter;
            this.getter = getter;
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

    static class PojoValues extends ArrayList<Object> {
        PojoValues(Collection<Object> values) {
            super(values);
        }
    }

    public interface FieldTypeConverter<T> {
        Type convert(T pojo, Type currType, int fieldIndex, int level, boolean isKey);
    }


}
