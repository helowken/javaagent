package agent.common.struct;

import agent.common.struct.impl.StructContext;

public interface StructField {
    byte getType();

    boolean matchType(Object value);

    Class<?>[] getValueClasses();

    int bytesSize(Object value, StructContext context);

    void serialize(BBuff bb, Object value, StructContext context);

    Object deserialize(BBuff bb, StructContext context);
}
