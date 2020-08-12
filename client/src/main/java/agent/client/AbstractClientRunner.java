package agent.client;

import agent.base.parser.CmdHelpException;
import agent.base.runner.Runner;
import agent.base.utils.*;
import agent.client.command.parser.CommandParserMgr;
import agent.client.command.parser.exception.CommandParseException;
import agent.client.command.result.CommandResultHandlerMgr;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.network.MessageIO;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

abstract class AbstractClientRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AbstractClientRunner.class);
    private static final String KEY_SOCKET_CONNECTION_TIMEOUT = "socket.connection.timeout";
    private static final Set<String> quitCmds = new HashSet<>(
            Arrays.asList("quit", "exit", "byte")
    );
    private static final CmdItem QUIT_ITEM = new CmdItem(null, true);
    private HostAndPort hostAndPort;

    static {
        Logger.setAsync(false);
    }

    abstract List<String> readCmdArgs() throws Exception;

    String[] init(Object[] args) {
        hostAndPort = Utils.getArgValue(args, 0);
        return Utils.getArgValue(args, 1);
    }

    @Override
    public void shutdown() {
    }

    private CmdItem parseCommand(List<String> argList) {
        if (argList == null || argList.isEmpty())
            return QUIT_ITEM;
        List<String> restArgList = new ArrayList<>(argList);
        String cmdName = restArgList.remove(0);
        if (quitCmds.contains(cmdName))
            return QUIT_ITEM;
        String[] args = restArgList.isEmpty() ?
                new String[0] :
                restArgList.toArray(new String[0]);
        return new CmdItem(
                CommandParserMgr.parse(cmdName, args),
                false
        );
    }

    List<String> splitStringToArgs(String line) {
        try {
            StringParser.CompiledStringExpr expr = StringParser.compile(line, "\"", "\"");
            List<String> rsList = new ArrayList<>();
            expr.getAllItems().forEach(
                    item -> {
                        String content = item.getContent().trim();
                        if (item.isKey())
                            rsList.add(content);
                        else
                            Collections.addAll(
                                    rsList,
                                    content.split("\\s+")
                            );
                    }
            );
            return rsList;
        } catch (Exception e) {
            throw new CommandParseException(
                    e.getMessage()
            );
        }
    }

    boolean connectTo() {
        try (Socket socket = new Socket()) {
            ConsoleLogger.getInstance().info("Try to connect to: {}:{}", hostAndPort.host, hostAndPort.port);
            socket.connect(
                    new InetSocketAddress(
                            hostAndPort.host,
                            hostAndPort.port
                    ),
                    Utils.parseInt(
                            SystemConfig.get(KEY_SOCKET_CONNECTION_TIMEOUT),
                            KEY_SOCKET_CONNECTION_TIMEOUT
                    )
            );
            ConsoleLogger.getInstance().info("Agent Server connected.");
            try (MessageIO io = MessageIO.create(socket)) {
                while (true) {
                    try {
                        CmdItem cmdItem = parseCommand(
                                readCmdArgs()
                        );
                        if (cmdItem.quit)
                            return true;
                        sendAndReceive(io, cmdItem.cmd);
                    } catch (CmdHelpException e) {
                        ConsoleLogger.getInstance().info("{}", e.getMessage());
                    } catch (CommandParseException e) {
                        ConsoleLogger.getInstance().error("Error: {}", e.getMessage());
                    } catch (Exception e) {
                        if (MessageIO.isNetworkException(e))
                            ConsoleLogger.getInstance().error("Disconnected from Agent Server.");
                        else {
                            logError("Error occurred.", e);
                        }
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            logError("Connect to server failed.", e);
            return true;
        }
    }

    private void logError(String msg, Exception e) {
        logger.error(msg, e);
        ConsoleLogger.getInstance().error(
                "{}\n{}",
                msg,
                Utils.getMergedErrorMessage(e)
        );
    }

    private void sendAndReceive(MessageIO io, Command cmd) throws Exception {
        io.send(cmd);
        ExecResult result = MessageMgr.parseResult(io.receive());
        CommandResultHandlerMgr.handleResult(cmd, result);
    }

    static class CmdItem {
        final Command cmd;
        final boolean quit;

        CmdItem(Command cmd, boolean quit) {
            this.cmd = cmd;
            this.quit = quit;
        }
    }
}
