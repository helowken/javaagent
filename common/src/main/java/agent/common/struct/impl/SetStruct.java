package agent.common.struct.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetStruct<V> extends CollectionStruct<V, Set<V>> {
    SetStruct() {
        super(StructFields.newSet(), new HashSet<>());
    }

    public Set<V> getAll() {
        return Collections.unmodifiableSet(getColl());
    }
}
