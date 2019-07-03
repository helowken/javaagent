package agent.common.message;

import agent.common.message.version.APIVersion;
import agent.common.struct.Struct;

public interface Message extends Struct {
    int getType();

    APIVersion getVersion();
}
