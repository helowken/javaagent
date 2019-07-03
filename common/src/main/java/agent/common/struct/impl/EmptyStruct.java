package agent.common.struct.impl;

import agent.common.struct.Struct;

import java.nio.ByteBuffer;

public class EmptyStruct implements Struct {
    private static final EmptyStruct instance = new EmptyStruct();

    static EmptyStruct getInstance() {
        return instance;
    }

    private EmptyStruct() {
    }

    @Override
    public void deserialize(ByteBuffer bb) {
    }

    @Override
    public void serialize(ByteBuffer bb) {
    }

    @Override
    public int bytesSize() {
        return 0;
    }
}
