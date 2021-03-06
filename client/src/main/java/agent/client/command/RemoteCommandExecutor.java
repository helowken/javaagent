package agent.client.command;

import agent.base.utils.*;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.CommandExecutor;
import agent.cmdline.command.result.ExecResult;
import agent.common.message.DefaultMessage;
import agent.common.network.MessageIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RemoteCommandExecutor implements CommandExecutor {
    private static final Logger logger = Logger.getLogger(RemoteCommandExecutor.class);
    private static final String KEY_SOCKET_CONNECTION_TIMEOUT = "socket.connection.timeout";
    private final HostAndPort hostAndPort;
    private Socket socket;
    private MessageIO msgIO;

    public RemoteCommandExecutor(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    @Override
    public ExecResult exec(Command cmd) {
        try {
            MessageIO io = getMsgIO();
            if (io != null) {
                io.send(
                        DefaultMessage.toMessage(cmd)
                );
                return io.receive().getBody();
            }
        } catch (Exception e) {
            if (MessageIO.isNetworkException(e)) {
                ConsoleLogger.getInstance().error("Disconnected from Agent Server.");
                shutdown();
            } else
                throw new RuntimeException(e);
        }
        return null;
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
                String msg = "Connect to server failed.";
                logger.error(msg, e);
                ConsoleLogger.getInstance().error(
                        "{}\n{}",
                        msg,
                        Utils.getMergedErrorMessage(e)
                );
                shutdown();
            }
        }
        return socket;
    }

    private synchronized MessageIO getMsgIO() throws IOException {
        if (msgIO == null) {
            Socket socket = getSocket();
            if (socket != null)
                msgIO = MessageIO.create(socket);
        }
        return msgIO;
    }

    public void shutdown() {
        if (msgIO != null) {
            msgIO.close();
            msgIO = null;
        }
        IOUtils.close(socket);
        socket = null;
    }

}
