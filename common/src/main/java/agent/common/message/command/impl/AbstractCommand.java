package agent.common.message.command.impl;

import agent.common.message.AbstractMessage;
import agent.common.message.command.Command;
import agent.common.struct.Struct;

abstract class AbstractCommand<T extends Struct> extends AbstractMessage<T> implements Command {
    AbstractCommand(int type, T body) {
        super(type, body);
    }
}
