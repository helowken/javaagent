package agent.common.struct.impl;

import agent.common.struct.BBuff;
import agent.common.struct.Struct;


public class EmptyStruct implements Struct {
    private static final EmptyStruct instance = new EmptyStruct();

    static EmptyStruct getInstance() {
        return instance;
    }

    private EmptyStruct() {
    }

    @Override
    public void deserialize(BBuff bb) {
    }

    @Override
    public void serialize(BBuff bb) {
    }

    @Override
    public int bytesSize() {
        return 0;
    }
}
