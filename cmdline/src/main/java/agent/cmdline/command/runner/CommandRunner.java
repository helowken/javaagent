package agent.cmdline.command.runner;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.CommandExecMgr;
import agent.cmdline.command.execute.CommandExecutor;
import agent.cmdline.command.parser.CommandParseMgr;
import agent.cmdline.command.parser.CommandParser;
import agent.cmdline.command.result.ExecResult;
import agent.cmdline.command.result.ExecResultHandler;
import agent.cmdline.command.result.ExecResultMgr;
import agent.cmdline.exception.CommandNotFoundException;
import agent.cmdline.exception.CommandParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CommandRunner {
    private static final Logger logger = Logger.getLogger(CommandRunner.class);
    private static final CommandRunner instance = new CommandRunner();
    private final CommandParseMgr cmdParseMgr = new CommandParseMgr();
    private final CommandExecMgr cmdExecMgr = new CommandExecMgr();
    private final ExecResultMgr execResultMgr = new ExecResultMgr();
    private Consumer<CommandNotFoundException> cmdNotFoundHandler;

    public static CommandRunner getInstance() {
        return instance;
    }

    public CommandRunner regParse(CommandParser... cmdParsers) {
        cmdParseMgr.reg(cmdParsers);
        return this;
    }

    public CommandRunner regParse(String sectionName, CommandParser... cmdParsers) {
        cmdParseMgr.reg(sectionName, cmdParsers);
        return this;
    }

    public CommandRunner regExec(CommandExecutor cmdExecutor, int... cmdTypes) {
        cmdExecMgr.reg(cmdExecutor, cmdTypes);
        return this;
    }

    public CommandRunner regResult(ExecResultHandler resultHandler, int... cmdTypes) {
        execResultMgr.reg(resultHandler, cmdTypes);
        return this;
    }

    public CommandRunner setDefaultExecutor(CommandExecutor cmdExecutor) {
        cmdExecMgr.setDefaultExecutor(cmdExecutor);
        return this;
    }

    public CommandRunner setCmdNotFoundHandler(Consumer<CommandNotFoundException> handler) {
        this.cmdNotFoundHandler = handler;
        return this;
    }

    public CommandExecMgr getCmdExecMgr() {
        return cmdExecMgr;
    }

    public ExecResultMgr getExecResultMgr() {
        return execResultMgr;
    }

    public CommandParseMgr getCmdParseMgr() {
        return cmdParseMgr;
    }

    private boolean execCmd(CmdItem cmdItem) {
        cmdItem.print();
        Command cmd = cmdItem.getCmd();
        ExecResult result = cmdExecMgr.exec(cmd);
        if (result == null)
            return false;
        execResultMgr.handleResult(cmd, result);
        return true;
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
                "{}",
                e.getMessage()
        );
        if (cmdNotFoundHandler != null)
            cmdNotFoundHandler.accept(e);
    }

    protected void handleCmdParseError(CommandParseException e) {
        ConsoleLogger.getInstance().error("{}", e.getMessage());
    }

    protected void handleError(Exception e) {
        String msg = "Error occurred.";
        logger.error(msg, e);
        ConsoleLogger.getInstance().error(
                "{}\n{}",
                msg,
                Utils.getMergedErrorMessage(e)
        );
    }

    protected void onExit() {
    }

}
