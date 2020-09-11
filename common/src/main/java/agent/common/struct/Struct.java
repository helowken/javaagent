package agent.common.struct;

public interface Struct {
    void deserialize(BBuff bb);

    void serialize(BBuff bb);

    int bytesSize();
}
