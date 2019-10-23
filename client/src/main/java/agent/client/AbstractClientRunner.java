package agent.client;

import agent.base.runner.Runner;
import agent.base.utils.Logger;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;
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

abstract class AbstractClientRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AbstractClientRunner.class);
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";
    private static final String CMD_QUIT = "quit";
    private static final CmdItem QUIT_ITEM = new CmdItem(null, true);

    abstract String readCmdLine() throws Exception;

    @Override
    public void shutdown() {
    }

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

    boolean connectTo() {
        String host = Optional.ofNullable(
                SystemConfig.get(KEY_HOST)
        ).orElse("127.0.0.1");
        int port = SystemConfig.getInt(KEY_PORT);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port));
            getClientLogger().info("Agent Server connected.");
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
                        getClientLogger().error(e.getMessage());
                    } catch (Exception e) {
                        if (MessageIO.isNetworkException(e))
                            getClientLogger().error("Disconnected from Agent Server.");
                        else {
                            logError("Error occurred.", e);
                        }
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            logError("Connect to server failed: {}", e);
            return true;
        }
    }

    private void logError(String msg, Exception e) {
        logger.error(msg, e);
        getClientLogger().error(
                msg,
                "\n" + Utils.getMergedErrorMessage(e)
        );
    }

    private void sendAndReceive(MessageIO io, Command cmd) throws Exception {
        io.send(cmd);
        ExecResult result = MessageMgr.parseResult(io.receive());
        CommandResultHandlerMgr.handleResult(cmd, result);
    }

    Logger getClientLogger() {
        return ClientLogger.logger;
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
