package agent.common.struct;

public interface StructField {
    boolean matchType(Object value);

    int bytesSize(Object value);

    void serialize(BBuff bb, Object value);

    Object deserialize(BBuff bb);

    Class<?> getValueClass();
}
