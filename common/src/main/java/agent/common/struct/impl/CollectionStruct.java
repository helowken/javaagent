package agent.common.struct.impl;

import agent.common.struct.Struct;
import agent.common.struct.StructField;

import java.nio.ByteBuffer;
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
    public void deserialize(ByteBuffer bb) {
        coll.addAll((Collection) field.deserialize(bb));
    }

    @Override
    public void serialize(ByteBuffer bb) {
        field.serialize(bb, coll);
    }

    @Override
    public int bytesSize() {
        return field.bytesSize(coll);
    }
}
