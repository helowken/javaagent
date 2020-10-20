package agent.common.struct.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListStruct<V> extends CollectionStruct<V, List<V>> {
    ListStruct() {
        super(StructFields.newList(), new ArrayList<>());
    }

    public List<V> getAll() {
        return Collections.unmodifiableList(getColl());
    }
}
