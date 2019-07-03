package agent.common.struct;

import java.nio.ByteBuffer;

public interface Struct {
    void deserialize(ByteBuffer bb);

    void serialize(ByteBuffer bb);

    int bytesSize();
}
