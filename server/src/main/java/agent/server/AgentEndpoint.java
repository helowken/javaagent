package agent.server;

import agent.server.command.executor.CmdExecutorMgr;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.network.MessageIO;
import agent.base.utils.Logger;

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

    private boolean receiveAndSend(MessageIO io) throws Exception {
        try {
            Command cmd = MessageMgr.parse(io.receive());
            ExecResult result = CmdExecutorMgr.exec(cmd);
            io.send(result);
        } catch (Exception e) {
            if (MessageIO.isNetworkException(e))
                return false;
            else {
                logger.error("receiveAndSend failed.", e);
                io.send(DefaultExecResult.toRuntimeError(e.getMessage()));
            }
        }
        return true;
    }

}
