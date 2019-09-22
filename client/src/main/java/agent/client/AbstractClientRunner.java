package agent.client;

import agent.base.utils.SystemConfig;
import agent.client.command.parser.CommandParserMgr;
import agent.client.command.parser.exception.CommandParseException;
import agent.client.command.result.handler.CommandResultHandlerMgr;
import agent.client.utils.ClientLogger;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.network.MessageIO;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;

abstract class AbstractClientRunner {
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";
    private static final String CMD_QUIT = "quit";
    private static final CmdItem QUIT_ITEM = new CmdItem(null, true);

    abstract String readCmdLine() throws Exception;

    private CmdItem parseCommand(String cmdLine) {
        String line = cmdLine;
        if (line != null &&
                !(line = line.trim()).isEmpty() &&
                !line.equals(CMD_QUIT)) {
            String[] args = line.split("\\s+");
            String cmdName = args[0];
            args = args.length > 1 ?
                    Arrays.copyOfRange(args, 1, args.length) :
                    new String[0];
            return new CmdItem(
                    CommandParserMgr.parse(cmdName, args),
                    false
            );
        }
        return QUIT_ITEM;
    }

    boolean connectTo() throws Exception {
        String host = Optional.ofNullable(
                SystemConfig.get(KEY_HOST)
        ).orElse("127.0.0.1");
        int port = SystemConfig.getInt(KEY_PORT);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port));
            ClientLogger.logger.info("Agent Server connected.");
            try (MessageIO io = MessageIO.create(socket)) {
                while (true) {
                    try {
                        CmdItem cmdItem = parseCommand(
                                readCmdLine()
                        );
                        if (cmdItem.quit)
                            return true;
                        sendAndReceive(io, cmdItem.cmd);
                    } catch (CommandParseException e) {
                        ClientLogger.logger.error(e.getMessage());
                    } catch (Exception e) {
                        if (MessageIO.isNetworkException(e))
                            ClientLogger.logger.error("Disconnected from Agent Server.");
                        else
                            ClientLogger.logger.error("Error occurred.", e);
                        return false;
                    }
                }
            }
        }
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
