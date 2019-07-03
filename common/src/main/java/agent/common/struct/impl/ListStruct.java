package agent.common.struct.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ListStruct<V> extends CollectionStruct<V, List<V>> {
    ListStruct() {
        super(StructFields.newList(), new LinkedList<>());
    }

    public List<V> getAll() {
        return Collections.unmodifiableList(getColl());
    }
}
