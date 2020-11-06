package agent.server;

import agent.base.utils.Logger;
import agent.common.message.DefaultMessage;
import agent.common.message.command.Command;
import agent.common.message.result.entity.DefaultExecResult;
import agent.common.network.MessageIO;
import agent.server.command.executor.CmdExecutorMgr;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class AgentEndpoint implements Runnable {
    private static final Logger logger = Logger.getLogger(AgentEndpoint.class);
    private Socket socket;

    AgentEndpoint(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            logger.debug("Client {} is connected.", socket.getRemoteSocketAddress());
            try (MessageIO io = MessageIO.create(socket)) {
                while (receiveAndSend(io)) ;
            }
        } catch (SocketException e) {
        } catch (Exception e) {
            logger.error("User agent instrumentError.", e);
        } finally {
            close();
        }
    }

    private void close() {
        try {
            logger.debug("Close socket.");
            socket.close();
        } catch (IOException e) {
        }
    }

    private boolean receiveAndSend(MessageIO io) {
        try {
            Command cmd = io.receive().getBody();
            io.send(
                    DefaultMessage.toMessage(
                            CmdExecutorMgr.exec(cmd)
                    )
            );
        } catch (Exception e) {
            if (MessageIO.isNetworkException(e))
                return false;
            else {
                logger.error("receiveAndSend failed.", e);
                try {
                    io.send(
                            DefaultMessage.toMessage(
                                    DefaultExecResult.toRuntimeError(
                                            e.getMessage()
                                    )
                            )
                    );
                } catch (Exception e2) {
                    logger.error("receiveAndSend reply error failed.", e2);
                    return false;
                }
            }
        }
        return true;
    }
}
