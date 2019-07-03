package agent.common.message;

import agent.common.message.version.APIVersion;
import agent.common.struct.Struct;
import agent.common.struct.impl.CompoundStruct;
import agent.common.struct.impl.DefaultStruct;
import agent.common.struct.impl.Structs;

public class AbstractMessage<T extends Struct> extends StructMessage<CompoundStruct> {
    private DefaultStruct messageType = Structs.newInt();
    private APIVersion apiVersion = APIVersion.getInstance();
    private T body;

    protected AbstractMessage(int type, T body) {
        this.body = body;
        initStruct();
        messageType.set(type);
    }

    protected T getBody() {
        return body;
    }

    @Override
    public int getType() {
        return messageType.get();
    }

    @Override
    protected CompoundStruct initStruct() {
        return new CompoundStruct(messageType, apiVersion, body);
    }

    public APIVersion getVersion() {
        return apiVersion;
    }
}
