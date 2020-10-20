package agent.common.struct.impl;

import agent.common.struct.BBuff;
import agent.common.struct.Struct;
import agent.common.struct.StructField;

import java.util.Collection;

@SuppressWarnings("unchecked")
abstract class CollectionStruct<V, T extends Collection<V>> implements Struct {
    private final StructField field;
    private final T coll;

    CollectionStruct(StructField field, T coll) {
        this.field = field;
        this.coll = coll;
    }

    public void add(V v) {
        coll.add(v);
    }

    public void addAll(Collection<V> vs) {
        coll.addAll(vs);
    }

    public void remove(V v) {
        coll.remove(v);
    }

    public void removeAll(Collection<V> vs) {
        coll.removeAll(vs);
    }

    public void clear() {
        coll.clear();
    }

    public int size() {
        return coll.size();
    }

    protected T getColl() {
        return coll;
    }

    @Override
    public void deserialize(BBuff bb) {
        Object v = field.deserialize(bb);
        if (v != null)
            coll.addAll((Collection) v);
    }

    @Override
    public void serialize(BBuff bb) {
        field.serialize(bb, coll);
    }

    @Override
    public int bytesSize() {
        return field.bytesSize(coll);
    }
}
