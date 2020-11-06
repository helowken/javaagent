package agent.common.struct.impl;

import agent.base.utils.Utils;
import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class StructContext {
    private final Map<Integer, PojoConfig> pojoConfigMap = new ConcurrentHashMap<>();
    private PojoCreator pojoCreator = null;
    private final Map<Object, PojoValues> cache = new ConcurrentHashMap<>();

    public void clear() {
        pojoConfigMap.clear();
        pojoCreator = null;
        clearCache();
    }

    public void clearCache() {
        cache.clear();
    }

    public <T> void addPojoInfo(Class<T> pojoClass, PojoInfo<T> pojoInfo) {
        addPojoInfo(pojoClass::equals, pojoInfo);
    }

    public <T> StructContext addPojoInfo(Predicate<Class<T>> predicate, PojoInfo<T> pojoInfo) {
        if (pojoInfo == null)
            throw new IllegalArgumentException();
        pojoConfigMap.compute(
                pojoInfo.getType(),
                (pojoType, oldValue) -> {
                    if (oldValue != null)
                        throw new RuntimeException("Duplicated pojo type: " + pojoType);
                    return new PojoConfig<>(predicate, pojoInfo);
                }
        );
        return this;
    }

    public synchronized void setPojoCreator(PojoCreator pc) {
        pojoCreator = pc;
    }

    Object createPojo(PojoValues pojoValues) {
        Object pojo;
        final int type = pojoValues.type;
        synchronized (this) {
            pojo = pojoCreator != null ?
                    pojoCreator.create(type) :
                    null;
        }
        if (pojo == null) {
            PojoConfig config = pojoConfigMap.get(type);
            if (config != null)
                pojo = config.pojoInfo.newPojo();
        }
        if (pojo != null)
            setPojoValues(pojo, pojoValues);
        return pojo;
    }

    private <T> void setPojoValues(T pojo, List<Object> fieldValues) {
        List<PojoFieldProperty<T>> fieldPropertyList = (List) getPojoInfo(pojo.getClass()).getAllFieldProperties();
        Utils.wrapToRtError(
                () -> {
                    Object fieldValue;
                    int idx = 0;
                    final int size = fieldValues.size();
                    for (PojoFieldProperty<T> fieldProperty : fieldPropertyList) {
                        if (idx >= size)
                            break;
                        fieldValue = convertValue(
                                fieldProperty.type,
                                fieldValues.get(idx++)
                        );
                        fieldProperty.setter.invoke(pojo, fieldValue);
                    }
                }
        );
    }

    private Object convertValue(Type currType, Object originalValue) {
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
                            convertValue(elClass, el)
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

                Map<Object, Object> rsMap = (Map) newInstanceForType(currClass);
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) originalValue).entrySet()) {
                    rsMap.put(
                            convertValue(keyType, entry.getKey()),
                            convertValue(valueType, entry.getValue())
                    );
                }
                return rsMap;
            } else if (Collection.class.isAssignableFrom(currClass)) {
                Type valueType = null;
                if (argTypes != null && argTypes.length >= 1)
                    valueType = argTypes[0];
                Collection<Object> rsColl = (Collection) newInstanceForType(currClass);
                for (Object value : ((Collection) originalValue)) {
                    rsColl.add(
                            convertValue(valueType, value)
                    );
                }
                return rsColl;
            }
        }

        if (isPojoValues(originalValue)) {
            PojoValues pojoValues = (PojoValues) originalValue;
            Object pojoOfField = createPojo(pojoValues);
            if (pojoOfField != null)
                return pojoOfField;
        }
        return originalValue;
    }

    PojoValues getPojoValues(Object v) {
        if (v == null)
            throw new IllegalArgumentException();
        return cache.computeIfAbsent(
                v,
                o -> {
                    PojoInfo<?> pojoInfo = getPojoInfo(o.getClass());
                    List<Object> values = pojoInfo.getAllFieldProperties()
                            .stream()
                            .map(
                                    fieldProperty -> Utils.wrapToRtError(
                                            () -> fieldProperty.getter.invoke(o)
                                    )
                            )
                            .collect(
                                    Collectors.toList()
                            );
                    return new PojoValues(
                            pojoInfo.getType(),
                            values
                    );
                }
        );
    }

    private boolean isPojoValues(Object value) {
        return value instanceof PojoValues;
    }

    private Object newInstanceForType(Class<?> clazz) {
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

    private <T> PojoInfo<T> getPojoInfo(Class<T> pojoClass) {
        for (PojoConfig config : pojoConfigMap.values()) {
            if (config.predicate.test(pojoClass))
                return config.pojoInfo;
        }

        PojoClass pojoAnnt = pojoClass.getAnnotation(PojoClass.class);
        if (pojoAnnt == null)
            throw new RuntimeException("No pojo class annotation found on class: " + pojoClass);
        int pojoType = pojoAnnt.type();

        Set<String> nameSet = new HashSet<>();
        List<PojoFieldPropertyList<T>> fieldPropertiesList = new ArrayList<>();
        Class<?> clazz = pojoClass;
        while (clazz != null) {
            fieldPropertiesList.add(
                    getFieldPropertyList(clazz, nameSet)
            );
            clazz = clazz.getSuperclass();
        }
        PojoInfo<T> pojoInfo = new PojoInfo<>(
                pojoType,
                () -> Utils.wrapToRtError(pojoClass::newInstance),
                fieldPropertiesList
        );
        addPojoInfo(pojoClass::equals, pojoInfo);
        return pojoInfo;
    }

    private <T> PojoFieldPropertyList<T> getFieldPropertyList(Class<?> clazz, Set<String> nameSet) {
        List<PojoFieldProperty<T>> fieldPropertyList = new ArrayList<>();
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
                            new PojoFieldProperty(
                                    field.getGenericType(),
                                    index,
                                    getMethod(clazz, propertyName, field.getType())::invoke,
                                    getMethod(clazz, propertyName, null)::invoke
                            )
                    );
                }
            }
        }
        return new PojoFieldPropertyList<>(fieldPropertyList);
    }

    private Method getMethod(Class<?> clazz, String propertyName, Class<?> fieldClass) {
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

    private static class PojoConfig<T> {
        final Predicate<Class<T>> predicate;
        final PojoInfo pojoInfo;

        private PojoConfig(Predicate<Class<T>> predicate, PojoInfo pojoInfo) {
            this.predicate = predicate;
            this.pojoInfo = pojoInfo;
        }
    }

}
