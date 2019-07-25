package agent.client;

import agent.base.utils.IOUtils;
import agent.base.utils.SystemConfig;
import agent.client.command.parser.CommandParserMgr;
import agent.client.command.parser.exception.CommandParseException;
import agent.client.command.result.handler.CommandResultHandlerMgr;
import agent.client.utils.ClientLogger;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.network.MessageIO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;

public class AgentClientRunner {
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";
    private static BufferedReader reader;

    public static void run() throws Exception {
        String host = Optional.ofNullable(
                SystemConfig.get(KEY_HOST)
        ).orElse("127.0.0.1");
        int port = SystemConfig.getInt(KEY_PORT);
        init();
        try {
            while (true) {
                if (connectTo(host, port))
                    break;
                else
                    ClientLogger.logger.info("Try to reconnect...");
            }
        } finally {
            IOUtils.close(reader);
        }
    }

    public static void shutdown() {
    }

    private static void init() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    private static boolean connectTo(String host, int port) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port));
            ClientLogger.logger.info("Agent Server connected.");
            try (MessageIO io = MessageIO.create(socket)) {
                while (true) {
                    try {
                        Command cmd = readCommand(reader);
                        if (cmd == null)
                            return true;
                        sendAndReceive(io, cmd);
                    } catch (CommandParseException e) {
                        ClientLogger.logger.error(e.getMessage());
                    } catch (Exception e) {
                        if (MessageIO.isNetworkException(e))
                            ClientLogger.logger.error("Disconnected from Agent Server.");
                        else
                            ClientLogger.logger.error("Error occurred.", e);
                        break;
                    }
                }
            }
        }
        return false;
    }

    private static Command readCommand(BufferedReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                if (line.equals("quit"))
                    break;
                else {
                    String[] args = line.split("\\s+");
                    String cmdName = args[0];
                    args = args.length > 1 ?
                            Arrays.copyOfRange(args, 1, args.length)
                            : new String[0];
                    return CommandParserMgr.parse(cmdName, args);
                }
            }
        }
        return null;
    }

    private static void sendAndReceive(MessageIO io, Command cmd) throws Exception {
        io.send(cmd);
        ExecResult result = MessageMgr.parseResult(io.receive());
        CommandResultHandlerMgr.handleResult(cmd, result);
    }

}
