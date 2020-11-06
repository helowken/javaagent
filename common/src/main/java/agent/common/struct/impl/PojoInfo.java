package agent.common.struct.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class PojoInfo<T> {
    private final int type;
    private final List<PojoFieldPropertyList<T>> fieldPropertiesList;
    private final Supplier<T> newObjFunc;

    public PojoInfo(int type, Supplier<T> newObjFunc, PojoFieldPropertyList<T> fieldProperties) {
        this(type, newObjFunc, Collections.singletonList(fieldProperties));
    }

    public PojoInfo(int type, Supplier<T> newObjFunc, List<PojoFieldPropertyList<T>> fieldPropertiesList) {
        this.type = type;
        this.newObjFunc = newObjFunc;
        this.fieldPropertiesList = new ArrayList<>(fieldPropertiesList);
    }

    T newPojo() {
        return newObjFunc == null ? null : newObjFunc.get();
    }

    int getType() {
        return type;
    }

    List<PojoFieldProperty<T>> getAllFieldProperties() {
        List<PojoFieldProperty<T>> rsList = new ArrayList<>();
        fieldPropertiesList.forEach(
                fieldProperties -> rsList.addAll(
                        fieldProperties.getPropertyList()
                )
        );
        return rsList;
    }

}

