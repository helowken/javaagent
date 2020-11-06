package agent.common.struct.impl;

public interface PojoFieldValueGetter<T, V> {
    V invoke(T obj) throws Exception;
}
