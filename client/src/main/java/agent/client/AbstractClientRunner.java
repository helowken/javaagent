package agent.client;

import agent.base.help.HelpInfo;
import agent.base.runner.Runner;
import agent.base.utils.*;
import agent.client.command.parser.CommandParserMgr;
import agent.client.command.parser.exception.CommandNotFoundException;
import agent.client.command.parser.exception.CommandParseException;
import agent.client.command.result.CommandResultHandlerMgr;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.network.MessageIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

abstract class AbstractClientRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AbstractClientRunner.class);
    private static final String KEY_SOCKET_CONNECTION_TIMEOUT = "socket.connection.timeout";
    private static final Set<String> quitCmds = new HashSet<>(
            Arrays.asList("q", "quit", "exit", "byte")
    );
    private SocketMgr socketMgr;

    static {
        Logger.setAsync(false);
    }

    abstract List<String> readCmdArgs() throws Exception;

    String[] init(Object[] args) {
        socketMgr = new SocketMgr(
                Utils.getArgValue(args, 0)
        );
        return Utils.getArgValue(args, 1);
    }

    @Override
    public void shutdown() {
        socketMgr.close();
    }

    private boolean isQuit(List<String> argList) {
        return argList == null ||
                argList.isEmpty() ||
                quitCmds.contains(
                        argList.get(0)
                );
    }

    private Command parseCommand(List<String> argList) {
        try {
            return CommandParserMgr.parse(
                    argList.get(0),
                    argList.subList(1, argList.size())
                            .toArray(new String[0])
            );
        } catch (CommandNotFoundException e) {
            ConsoleLogger.getInstance().error(
                    "{}\nType 'ja help' to get a list of global options and commands.",
                    e.getMessage()
            );
        } catch (CommandParseException e) {
            ConsoleLogger.getInstance().error("{}", e.getMessage());
        }
        return null;
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

    boolean execCmd() {
        while (true) {
            try {
                List<String> argList = readCmdArgs();
                if (isQuit(argList))
                    return true;

                Command cmd = parseCommand(argList);
                if (cmd == null)
                    return false;

                socketMgr.sendAndReceive(
                        io -> {
                            io.send(cmd);
                            ExecResult result = MessageMgr.parseResult(
                                    io.receive()
                            );
                            CommandResultHandlerMgr.handleResult(cmd, result);
                        }
                );
            } catch (Exception e) {
                logError("Error occurred.", e);
                return false;
            }
        }
    }

    private static void logError(String msg, Exception e) {
        logger.error(msg, e);
        ConsoleLogger.getInstance().error(
                "{}\n{}",
                msg,
                Utils.getMergedErrorMessage(e)
        );
    }

    public List<HelpInfo> getHelps() {
        return CommandParserMgr.getHelps();
    }

    private static class SocketMgr {
        private final HostAndPort hostAndPort;
        private Socket socket;
        private MessageIO msgIO;

        private SocketMgr(HostAndPort hostAndPort) {
            this.hostAndPort = hostAndPort;
        }

        private Socket getSocket() {
            if (socket == null) {
                try {
                    socket = new Socket();
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
                } catch (Exception e) {
                    logError("Connect to server failed.", e);
                    close();
                }
            }
            return socket;
        }

        synchronized MessageIO getMsgIO() throws IOException {
            if (msgIO == null) {
                Socket socket = getSocket();
                if (socket != null)
                    msgIO = MessageIO.create(socket);
            }
            return msgIO;
        }

        void sendAndReceive(MsgIOFunc func) throws Exception {
            try {
                MessageIO io = getMsgIO();
                if (io != null)
                    func.exec(io);
            } catch (Exception e) {
                if (MessageIO.isNetworkException(e)) {
                    ConsoleLogger.getInstance().error("Disconnected from Agent Server.");
                    close();
                } else
                    throw e;
            }
        }

        private void close() {
            if (msgIO != null) {
                msgIO.close();
                msgIO = null;
            }
            IOUtils.close(socket);
            socket = null;
        }
    }

    private interface MsgIOFunc {
        void exec(MessageIO msgIO) throws Exception;
    }
}
