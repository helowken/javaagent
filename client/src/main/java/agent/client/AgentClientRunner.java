package agent.client;

import agent.base.runner.Runner;
import agent.base.utils.*;
import agent.client.command.parser.CmdHelpUtils;
import agent.client.command.parser.CmdItem;
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
import java.util.List;

public class AgentClientRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AgentClientRunner.class);
    private static final String KEY_SOCKET_CONNECTION_TIMEOUT = "socket.connection.timeout";
    private static final AgentClientRunner instance = new AgentClientRunner();
    private SocketMgr socketMgr;

    static {
        Logger.setAsync(false);
    }

    public static AgentClientRunner getInstance() {
        return instance;
    }

    private AgentClientRunner() {
    }

    @Override
    public void startup(Object... args) {
        socketMgr = new SocketMgr(
                Utils.getArgValue(args, 0)
        );
        CmdHelpUtils.setOptConfigList(
                Utils.getArgValue(args, 1)
        );
        List<String> cmdArgs = Utils.getArgValue(args, 2);
        logger.debug("Cmd args: {}", cmdArgs);
        execCmd(cmdArgs);
    }

    @Override
    public void shutdown() {
        socketMgr.close();
    }

    private Command parseCommand(List<String> argList) {
        CmdItem cmdItem = CommandParserMgr.parse(
                argList.get(0),
                argList.subList(1, argList.size())
                        .toArray(new String[0])
        );
        if (cmdItem.isHelp()) {
            printHelp(cmdItem);
            return null;
        }
        return cmdItem.getCmd();
    }

    private void printHelp(CmdItem cmdItem) {
        StringBuilder sb = new StringBuilder();
        cmdItem.getHelpInfo().print(sb);
        ConsoleLogger.getInstance().info("{}", sb);
    }

    public boolean execCmd(List<String> argList) {
        try {
            Command cmd = parseCommand(argList);
            if (cmd != null)
                socketMgr.sendAndReceive(
                        io -> {
                            io.send(cmd);
                            ExecResult result = MessageMgr.parse(
                                    io.receive()
                            );
                            CommandResultHandlerMgr.handleResult(cmd, result);
                        }
                );
            return true;
        } catch (CommandNotFoundException e) {
            ConsoleLogger.getInstance().error(
                    "{}\n{}",
                    e.getMessage(),
                    "Type 'ja help' to get a list of global options and commands."
            );
        } catch (CommandParseException e) {
            ConsoleLogger.getInstance().error("{}", e.getMessage());
        } catch (Exception e) {
            logError("Error occurred.", e);
        }
        return false;
    }

    private static void logError(String msg, Exception e) {
        logger.error(msg, e);
        ConsoleLogger.getInstance().error(
                "{}\n{}",
                msg,
                Utils.getMergedErrorMessage(e)
        );
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
                    ConsoleLogger.getInstance().info("Connect to: {}:{}", hostAndPort.host, hostAndPort.port);
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