package agent.client.command;

import agent.base.utils.*;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.cmdline.command.result.ExecResult;
import agent.cmdline.command.result.ExecResultMgr;
import agent.cmdline.command.runner.AbstractCommandRunner;
import agent.common.message.DefaultMessage;
import agent.common.network.MessageIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientCmdRunner extends AbstractCommandRunner {
    private static final String KEY_SOCKET_CONNECTION_TIMEOUT = "socket.connection.timeout";
    private final ExecResultMgr execResultMgr = new ExecResultMgr();
    private SocketMgr socketMgr;

    public void init(HostAndPort hostAndPort) {
        socketMgr = new SocketMgr(hostAndPort);
    }

    public ExecResultMgr getExecResultMgr() {
        return execResultMgr;
    }

    @Override
    protected boolean execCmd(CmdItem cmdItem) throws Exception {
        cmdItem.print();
        Command cmd = cmdItem.getCmd();
        return socketMgr.sendAndReceive(
                io -> {
                    io.send(
                            DefaultMessage.toMessage(cmd)
                    );
                    ExecResult result = io.receive().getBody();
                    execResultMgr.handleResult(cmd, result);
                }
        );
    }

    @Override
    protected void onExit() {
        socketMgr.close();
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
