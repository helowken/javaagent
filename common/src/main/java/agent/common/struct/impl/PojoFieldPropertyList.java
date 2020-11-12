package agent.common.struct.impl;

import java.util.*;

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
}