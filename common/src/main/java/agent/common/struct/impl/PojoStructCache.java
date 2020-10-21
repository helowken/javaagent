package agent.common.struct.impl;

import agent.base.utils.Utils;
import agent.common.utils.annotation.PojoProperty;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
class PojoStructCache {
    private static final Map<Class<?>, List<FieldProperty>> classToFieldProperties = new ConcurrentHashMap<>();
    private static final Comparator<FieldProperty> comparator = (o1, o2) -> {
        if (o1.index > o2.index)
            return 1;
        else if (o1.index < o2.index)
            return -1;
        return 0;
    };

    public static void clear() {
        classToFieldProperties.clear();
    }

    static void setPojoValues(Object o, List<Object> pojoValues) {
        List<FieldProperty> fieldPropertyList = getTotalFieldPropertyList(o.getClass());
        if (pojoValues.size() != fieldPropertyList.size())
            throw new RuntimeException("Pojo values size is: " + pojoValues.size() + ", but field list size is: " + fieldPropertyList.size());
        Utils.wrapToRtError(
                () -> {
                    FieldProperty fieldProperty;
                    for (int i = 0, len = fieldPropertyList.size(); i < len; ++i) {
                        fieldProperty = fieldPropertyList.get(i);
                        fieldProperty.setter.invoke(
                                o,
                                convertValue(
                                        fieldProperty.type,
                                        pojoValues.get(i)
                                )
                        );
                    }
                }
        );
    }

    private static Object tryToConvertPojo(Class<?> clazz, Object value) {
        if (isPojoValues(value)) {
            PojoValues pojoValues = (PojoValues) value;
            Object pojoOfField = newInstanceForType(
                    clazz,
                    pojoValues.size()
            );
            setPojoValues(pojoOfField, pojoValues);
            return pojoOfField;
        }
        return value;
    }

    private static Object convertValue(Type classType, Object o) {
        if (o == null)
            return null;

        Type[] argTypes = null;
        Class<?> clazz;
        if (classType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) classType;
            clazz = (Class<?>) paramType.getRawType();
            argTypes = paramType.getActualTypeArguments();
        } else {
            clazz = (Class<?>) classType;
        }

        if (clazz.isArray()) {
            int len = Array.getLength(o);
            Class<?> elClass = clazz.getComponentType();
            Object array = Array.newInstance(elClass, len);
            for (int i = 0; i < len; ++i) {
                Array.set(
                        array,
                        i,
                        tryToConvertPojo(
                                elClass,
                                Array.get(o, i)
                        )
                );
            }
            return array;
        } else if (Map.class.isAssignableFrom(clazz)) {
            if (argTypes != null && argTypes.length >= 2) {
                Map<Object, Object> rsMap = (Map) newInstanceForType(clazz, 0);
                Type keyClass = argTypes[0];
                Type valueClass = argTypes[1];
                ((Map) o).forEach(
                        (key, value) -> rsMap.put(
                                convertValue(keyClass, key),
                                convertValue(valueClass, value)
                        )
                );
                return rsMap;
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            if (argTypes != null && argTypes.length >= 1) {
                Collection<Object> rsColl = (Collection) newInstanceForType(clazz, 0);
                Class<?> elClass = (Class<?>) argTypes[0];
                ((Collection) o).forEach(
                        el -> rsColl.add(
                                convertValue(elClass, el)
                        )
                );
                return rsColl;
            }
        }

        return tryToConvertPojo(clazz, o);
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
        return classToFieldProperties.computeIfAbsent(
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
                                    field.getType(),
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

    static class FieldProperty {
        final Class<?> clazz;
        final Type type;
        final int index;
        final Method setter;
        final Method getter;

        private FieldProperty(Class<?> clazz, Type type, int index, Method setter, Method getter) {
            this.clazz = clazz;
            this.type = type;
            this.index = index;
            this.setter = setter;
            this.getter = getter;
        }

        @Override
        public String toString() {
            return "FieldProperty{" +
                    "class=" + clazz +
                    ", type=" + type +
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
}
