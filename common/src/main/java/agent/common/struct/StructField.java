package agent.common.struct;

import java.nio.ByteBuffer;

public interface StructField {
    boolean matchType(Object value);

    int bytesSize(Object value);

    void serialize(ByteBuffer bb, Object value);

    Object deserialize(ByteBuffer bb);

    Class<?> getValueClass();
}
