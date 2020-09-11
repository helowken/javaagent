package agent.common.message;

import agent.common.struct.BBuff;
import agent.common.struct.Struct;

public abstract class StructMessage<T extends Struct> implements Message {
    private volatile T struct;

    @Override
    public void deserialize(BBuff bb) {
        getStruct().deserialize(bb);
    }

    @Override
    public void serialize(BBuff bb) {
        getStruct().serialize(bb);
    }

    @Override
    public int bytesSize() {
        return getStruct().bytesSize();
    }

    protected T getStruct() {
        if (struct == null) {
            synchronized (this) {
                if (struct == null) {
                    struct = initStruct();
                }
            }
        }
        return struct;
    }

    protected abstract T initStruct();
}
