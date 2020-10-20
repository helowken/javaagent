package agent.common.struct;

public interface Convertible {
    int bytesSize(Object value);

    void serialize(BBuff bb, Object value);

    Object deserialize(BBuff bb);
}