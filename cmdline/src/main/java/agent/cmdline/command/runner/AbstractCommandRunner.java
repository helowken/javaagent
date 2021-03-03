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
    private final CommandParseMgr cmdParseMgr = new CommandParseMgr();

    protected abstract boolean execCmd(CmdItem item) throws Exception;

    public CommandParseMgr getCmdParseMgr() {
        return cmdParseMgr;
    }

    public void run(String[] args) {
        run(
                Arrays.asList(args)
        );
    }

    public void run(List<String> argList) {
        try {
            List<CmdItem> cmdItemList = parse(
                    new ArrayList<>(argList)
            );
            for (CmdItem cmdItem : cmdItemList) {
                if (cmdItem.isHelp())
                    printHelp(cmdItem);
                else {
                    Command cmd = cmdItem.getCmd();
                    if (cmd != null && !execCmd(cmdItem))
                        break;
                }
            }
        } catch (CommandNotFoundException e) {
            handleCmdNotFound(e);
        } catch (CommandParseException e) {
            handleCmdParseError(e);
        } catch (Exception e) {
            handleError(e);
        } finally {
            onExit();
        }
    }

    protected List<CmdItem> parse(List<String> argList) {
        return cmdParseMgr.parse(argList);
    }

    protected void printHelp(CmdItem cmdItem) {
        StringBuilder sb = new StringBuilder();
        cmdItem.getHelpInfo().print(sb);
        ConsoleLogger.getInstance().info("{}", sb);
    }

    protected void handleCmdNotFound(CommandNotFoundException e) {
        ConsoleLogger.getInstance().error(
                "{}\n{}",
                e.getMessage(),
                "Type 'ja help' to get a list of global options and commands."
        );
    }

    protected void handleCmdParseError(CommandParseException e) {
        ConsoleLogger.getInstance().error("{}", e.getMessage());
    }

    protected void handleError(Exception e) {
        logError("Error occurred.", e);
    }

    protected void onExit() {
    }

    protected static void logError(String msg, Exception e) {
        logger.error(msg, e);
        ConsoleLogger.getInstance().error(
                "{}\n{}",
                msg,
                Utils.getMergedErrorMessage(e)
        );
    }


}
