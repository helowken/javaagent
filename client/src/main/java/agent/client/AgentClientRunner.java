package agent.client;

import agent.base.runner.Runner;
import agent.base.utils.*;
import agent.client.command.parser.ClientCommandParserMgr;
import agent.client.command.parser.CmdHelpUtils;
import agent.client.command.result.CommandResultHandlerMgr;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.cmdline.exception.CommandNotFoundException;
import agent.cmdline.exception.CommandParseException;
import agent.common.message.DefaultMessage;
import agent.common.message.result.ExecResult;
import agent.common.network.MessageIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class AgentClientRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AgentClientRunner.class);
    private static final String KEY_SOCKET_CONNECTION_TIMEOUT = "socket.connection.timeout";
    private SocketMgr socketMgr;

    static {
        Logger.setAsync(false);
    }

    @Override
    public void startup(Object... args) {
        socketMgr = new SocketMgr(
                Utils.getArgValue(args, 0)
        );
        CmdHelpUtils.setGlobalOptConfigList(
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

    private void printHelp(CmdItem cmdItem) {
        StringBuilder sb = new StringBuilder();
        cmdItem.getHelpInfo().print(sb);
        ConsoleLogger.getInstance().info("{}", sb);
    }

    private void execCmd(List<String> argList) {
        try {
            List<CmdItem> cmdItemList = ClientCommandParserMgr.getInstance().parse(argList);
            for (CmdItem cmdItem : cmdItemList) {
                if (cmdItem.isHelp())
                    printHelp(cmdItem);
                else {
                    Command cmd = cmdItem.getCmd();
                    if (cmd != null) {
                        cmdItem.print();
                        boolean rs = socketMgr.sendAndReceive(
                                io -> {
                                    io.send(
                                            DefaultMessage.toMessage(cmd)
                                    );
                                    ExecResult result = io.receive().getBody();
                                    CommandResultHandlerMgr.handleResult(cmd, result);
                                }
                        );
                        if (!rs)
                            break;
                    }
                }
            }
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
                    ConsoleLogger.getInstance().info("Connect to: {}", hostAndPort);
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

        boolean sendAndReceive(MsgIOFunc func) throws Exception {
            try {
                MessageIO io = getMsgIO();
                if (io != null) {
                    func.exec(io);
                    return true;
                }
            } catch (Exception e) {
                if (MessageIO.isNetworkException(e)) {
                    ConsoleLogger.getInstance().error("Disconnected from Agent Server.");
                    close();
                } else
                    throw e;
            }
            return false;
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
