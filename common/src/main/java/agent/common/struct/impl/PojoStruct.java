package agent.common.struct.impl;

import agent.base.utils.Utils;
import agent.common.struct.BBuff;
import agent.common.struct.Convertible;
import agent.common.struct.Struct;
import agent.common.utils.annotation.PojoProperty;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class PojoStruct implements Struct {
    private static final Map<Class<?>, List<FieldProperty>> classToFieldProperties = new ConcurrentHashMap<>();
    private static final Comparator<FieldProperty> comparator = (o1, o2) -> {
        if (o1.index > o2.index)
            return 1;
        else if (o1.index < o2.index)
            return -1;
        return 0;
    };

    private final Class<?> pojoClass;
    private final Convertible convertible;
    private Object pojo;

    public PojoStruct(Class<?> pojoClass) {
        if (StructFields.detectTypeByClass(pojoClass) != StructFields.T_UNKNOWN)
            throw new IllegalArgumentException("Unsupported class: " + pojoClass);
        this.pojoClass = pojoClass;
        this.convertible = convert(pojoClass);
    }

    public Object getPojo() {
        return pojo;
    }

    public void setPojo(Object pojo) {
        if (!pojoClass.isInstance(pojo))
            throw new IllegalArgumentException("Value for type: " + pojoClass + ", object: " + pojo);
        this.pojo = pojo;
    }

    @Override
    public void deserialize(BBuff bb) {
        pojo = convertible.deserialize(bb);
    }

    @Override
    public void serialize(BBuff bb) {
        convertible.serialize(bb, pojo);
    }

    @Override
    public int bytesSize() {
        return convertible.bytesSize(pojo);
    }

    private static Convertible convert(Class<?> clazz) {
        CompoundConvertible compoundConvertible = new CompoundConvertible(clazz);
        List<FieldProperty> fieldPropertyList = getTotalFieldPropertyList(clazz);
        for (FieldProperty fieldProperty : fieldPropertyList) {
            compoundConvertible.add(
                    convertTo(fieldProperty.type),
                    fieldProperty
            );
        }
        return compoundConvertible;
    }

    private static Convertible convertTo(Type classType) {
        Class<?> clazz;
        Type[] argTypes = null;
        if (classType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) classType;
            clazz = (Class<?>) paramType.getRawType();
            argTypes = paramType.getActualTypeArguments();
        } else {
            clazz = (Class<?>) classType;
        }

        if (clazz.isArray()) {
            Class<?> elClass = clazz.getComponentType();
            byte type = StructFields.detectTypeByClass(elClass);
            if (type == StructFields.T_UNKNOWN)
                return new ArrayConvertible(
                        elClass,
                        convertTo(elClass)
                );
        } else if (Map.class.isAssignableFrom(clazz)) {
            return argTypes != null && argTypes.length >= 2 ?
                    new MapConvertible(
                            clazz,
                            convertTo(argTypes[0]),
                            convertTo(argTypes[1])
                    ) :
                    StructFields.newMap(); // TODO use class to new instance
        } else if (Collection.class.isAssignableFrom(clazz)) {
            return argTypes != null && argTypes.length >= 1 ?
                    new CollectionConvertible(
                            clazz,
                            convertTo(argTypes[0])
                    ) :
                    StructFields.newList(); // TODO use class to new instance
        }

        byte type = StructFields.detectTypeByClass(clazz);
        return type == StructFields.T_UNKNOWN ?
                convert(clazz) :
                StructFields.getField(type);
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
        }
        fieldPropertyList.sort(comparator);
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

    private static Object newInstanceForType(Class<?> clazz) {
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
        return new ArrayList<>();
    }

    private static class CompoundConvertible implements Convertible {
        private final Map<Convertible, FieldProperty> convertibleToFieldProperty = new LinkedHashMap<>();
        private final Class<?> clazz;

        private CompoundConvertible(Class<?> clazz) {
            this.clazz = clazz;
        }

        private Object getPojoValue(FieldProperty fieldProperty, Object o) {
            return Utils.wrapToRtError(
                    () -> fieldProperty.getter.invoke(o)
            );
        }

        public void add(Convertible convertible, FieldProperty fieldProperty) {
            convertibleToFieldProperty.put(convertible, fieldProperty);
        }

        @Override
        public int bytesSize(Object value) {
            int size = 0;
            for (Map.Entry<Convertible, FieldProperty> entry : convertibleToFieldProperty.entrySet()) {
                size += entry.getKey().bytesSize(
                        getPojoValue(
                                entry.getValue(),
                                value
                        )
                );
            }
            return size;
        }

        @Override
        public void serialize(BBuff bb, Object value) {
            convertibleToFieldProperty.forEach(
                    (convertible, fieldProperty) -> convertible.serialize(
                            bb,
                            getPojoValue(
                                    fieldProperty,
                                    value
                            )
                    )
            );
        }

        @Override
        public Object deserialize(BBuff bb) {
            Object o = newInstanceForType(clazz);
            convertibleToFieldProperty.forEach(
                    (convertible, fieldProperty) -> Utils.wrapToRtError(
                            () -> fieldProperty.setter.invoke(
                                    o,
                                    convertible.deserialize(bb)
                            )
                    )
            );
            return o;
        }
    }

    private static class ArrayConvertible implements Convertible {
        private final Class<?> elClass;
        private final Convertible elConvertible;

        private ArrayConvertible(Class<?> elClass, Convertible elConvertible) {
            this.elClass = elClass;
            this.elConvertible = elConvertible;
        }

        @Override
        public int bytesSize(Object value) {
            int size = Integer.BYTES;
            if (value != null) {
                for (int i = 0, len = Array.getLength(value); i < len; ++i) {
                    size += elConvertible.bytesSize(
                            Array.get(value, i)
                    );
                }
            }
            return size;
        }

        @Override
        public void serialize(BBuff bb, Object value) {
            if (value != null) {
                int len = Array.getLength(value);
                bb.putInt(len);
                for (int i = 0; i < len; ++i) {
                    elConvertible.serialize(
                            bb,
                            Array.get(value, i)
                    );
                }
            } else
                bb.putInt(StructFields.T_NULL);
        }

        @Override
        public Object deserialize(BBuff bb) {
            int len = bb.getInt();
            if (len == StructFields.T_NULL)
                return null;
            Object array = Array.newInstance(elClass, len);
            for (int i = 0; i < len; ++i) {
                Array.set(
                        array,
                        i,
                        elConvertible.deserialize(bb)
                );
            }
            return array;
        }
    }

    private static class CollectionConvertible implements Convertible {
        private final Class<?> elClass;
        private final Convertible elConvertible;

        CollectionConvertible(Class<?> elClass, Convertible elConvertible) {
            this.elClass = elClass;
            this.elConvertible = elConvertible;
        }

        @Override
        public int bytesSize(Object value) {
            int size = Integer.BYTES;
            if (value != null) {
                Collection<Object> coll = (Collection) value;
                for (Object el : coll) {
                    size += elConvertible.bytesSize(el);
                }
            }
            return size;
        }

        @Override
        public void serialize(BBuff bb, Object value) {
            if (value == null)
                bb.putInt(StructFields.T_NULL);
            else {
                Collection<Object> coll = (Collection) value;
                bb.putInt(coll.size());
                for (Object el : coll) {
                    elConvertible.serialize(bb, el);
                }
            }
        }

        @Override
        public Object deserialize(BBuff bb) {
            int size = bb.getInt();
            if (size == StructFields.T_NULL)
                return null;
            Collection<Object> rsList = (Collection) newInstanceForType(elClass);
            for (int i = 0; i < size; ++i) {
                rsList.add(elConvertible.deserialize(bb));
            }
            return rsList;
        }
    }

    private static class MapConvertible implements Convertible {
        private final Class<?> clazz;
        private final Convertible keyConvertible;
        private final Convertible valueConvertible;

        MapConvertible(Class<?> clazz, Convertible keyConvertible, Convertible valueConvertible) {
            this.clazz = clazz;
            this.keyConvertible = keyConvertible;
            this.valueConvertible = valueConvertible;
        }

        @Override
        public int bytesSize(Object value) {
            Map<Object, Object> map = (Map) value;
            int size = Integer.BYTES;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                size += keyConvertible.bytesSize(entry.getKey());
                size += valueConvertible.bytesSize(entry.getValue());
            }
            return size;
        }

        @Override
        public void serialize(BBuff bb, Object value) {
            if (value == null)
                bb.putInt(StructFields.T_NULL);
            else {
                Map<Object, Object> map = (Map) value;
                bb.putInt(map.size());
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    keyConvertible.serialize(bb, entry.getKey());
                    valueConvertible.serialize(bb, entry.getValue());
                }
            }
        }

        @Override
        public Object deserialize(BBuff bb) {
            int size = bb.getInt();
            if (size == StructFields.T_NULL)
                return null;
            Map<Object, Object> map = (Map) newInstanceForType(clazz);
            for (int i = 0; i < size; ++i) {
                map.put(
                        keyConvertible.deserialize(bb),
                        valueConvertible.deserialize(bb)
                );
            }
            return map;
        }
    }

    private static class FieldProperty {
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
                    ",type=" + type +
                    ", index=" + index +
                    ", setter=" + setter +
                    ", getter=" + getter +
                    '}';
        }
    }
}
