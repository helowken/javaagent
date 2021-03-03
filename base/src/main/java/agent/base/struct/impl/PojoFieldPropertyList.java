package agent.base.struct.impl;

import agent.base.utils.Utils;

import java.util.*;
import java.util.function.Function;

public class PojoFieldPropertyList<T> {
    private static final Comparator<PojoFieldProperty> comparator = (o1, o2) -> {
        if (o1.index > o2.index)
            return 1;
        else if (o1.index < o2.index)
            return -1;
        return 0;
    };
    private List<PojoFieldProperty<T>> propertyList = new ArrayList<>();

    @SafeVarargs
    public PojoFieldPropertyList(PojoFieldProperty<T>... fieldProperties) {
        this(Arrays.asList(fieldProperties));
    }

    public PojoFieldPropertyList(Collection<PojoFieldProperty<T>> fieldProperties) {
        validate(fieldProperties);
        this.propertyList.addAll(fieldProperties);
        this.propertyList.sort(comparator);
    }

    List<PojoFieldProperty<T>> getPropertyList() {
        return Collections.unmodifiableList(propertyList);
    }

    private void validate(Collection<PojoFieldProperty<T>> fieldProperties) {
        Set<Integer> indexSet = new HashSet<>();
        fieldProperties.forEach(
                fieldProperty -> {
                    if (indexSet.contains(fieldProperty.index))
                        throw new RuntimeException("Duplicated field index: " + fieldProperty.index);
                    indexSet.add(fieldProperty.index);
                    if (fieldProperty.getter == null && fieldProperty.setter == null)
                        throw new RuntimeException("Getter and Setter can not both be null.");
                }
        );
    }

    public static <V> PojoFieldPropertyList<V> create(Class<V> clazz, String... fieldNames) {
        return create(clazz, null, null, fieldNames);
    }

    public static <V> PojoFieldPropertyList<V> create(Class<V> clazz, Function<String, String> fieldToPropertyFunc,
                                                      Function<String, Integer> indexFunc, String... fieldNames) {
        if (fieldNames == null || fieldNames.length == 0)
            throw new IllegalArgumentException("Field names can not be blank.");
        int idx = 0;
        int currIdx;
        String propertyName;
        List<PojoFieldProperty<V>> rsList = new ArrayList<>(fieldNames.length);
        for (String fieldName : fieldNames) {
            if (Utils.isBlank(fieldName))
                throw new IllegalArgumentException("Illegal field name: " + fieldName);
            currIdx = indexFunc == null ? idx++ : indexFunc.apply(fieldName);
            propertyName = fieldToPropertyFunc == null ? fieldName : fieldToPropertyFunc.apply(fieldName);
            rsList.add(
                    PojoFieldProperty.create(clazz, fieldName, propertyName, currIdx)
            );
        }
        return new PojoFieldPropertyList<>(rsList);
    }
}
