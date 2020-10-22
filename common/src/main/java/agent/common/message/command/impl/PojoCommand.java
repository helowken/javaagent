package agent.common.message.command.impl;

import agent.common.struct.impl.PojoStruct;
import agent.common.struct.impl.Structs;

public class PojoCommand extends AbstractCommand<PojoStruct> {
    public PojoCommand(int cmdType, Object pojo) {
        super(cmdType, Structs.newPojo());
        getBody().setPojo(pojo);
    }

    public <T> T getPojo() {
        return (T) getBody().getPojo();
    }
}
