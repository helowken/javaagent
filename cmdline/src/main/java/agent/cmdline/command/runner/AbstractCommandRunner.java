package agent.cmdline.command.runner;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.cmdline.command.parser.CommandParseMgr;
import agent.cmdline.exception.CommandNotFoundException;
import agent.cmdline.exception.CommandParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCommandRunner {
    private static final Logger logger = Logger.getLogger(AbstractCommandRunner.class);

    protected abstract boolean execCmd(CmdItem item) throws Exception;



}
