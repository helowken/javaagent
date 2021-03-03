package agent.base.struct.impl;

public interface PojoFieldValueSetter<T, V> {
    void invoke(T obj, V value) throws Exception;
}
