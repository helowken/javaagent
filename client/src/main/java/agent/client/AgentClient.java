package agent.client;

import agent.base.utils.Utils;
import agent.client.command.parser.CommandParserMgr;
import agent.client.command.parser.exception.CommandParseException;
import agent.client.command.result.handler.CommandResultHandlerMgr;
import agent.client.command.result.handler.TestConfigResultHandler;
import agent.client.command.result.handler.ViewResultHandler;
import agent.common.message.MessageMgr;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.network.MessageIO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import static agent.common.message.command.CommandType.CMD_TYPE_TEST_CONFIG;
import static agent.common.message.command.CommandType.CMD_TYPE_VIEW;

public class AgentClient {
    private static final String PREFIX = "[SYS]: ";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: [host] port");
            System.exit(-1);
        }
        String host;
        int port;
        if (args.length == 2) {
            host = args[0];
            port = Utils.parseInt(args[1], "port");
        } else {
            host = "127.0.0.1";
            port = Utils.parseInt(args[0], "port");
        }
        connectTo(host, port);
    }

    private static void init() {
        CommandResultHandlerMgr.regResultHandlerClass(CMD_TYPE_TEST_CONFIG, new TestConfigResultHandler());
        CommandResultHandlerMgr.regResultHandlerClass(CMD_TYPE_VIEW, new ViewResultHandler());
    }

    private static void connectTo(String host, int port) throws Exception {
        try (Socket socket = new Socket();
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))
        ) {
            socket.connect(new InetSocketAddress(host, port));
            init();
            info("Agent Server connected.");
            try (MessageIO io = MessageIO.create(socket)) {
                while (true) {
                    try {
                        Command cmd = readCommand(reader);
                        if (cmd == null)
                            break;
                        sendAndReceive(io, cmd);
                    } catch (CommandParseException e) {
                        error(e.getMessage());
                    } catch (Exception e) {
                        if (MessageIO.isNetworkException(e))
                            error("Disconnected from Agent Server.");
                        else
                            e.printStackTrace();
                        break;
                    }
                }
            }
        }
    }

    private static void info(String msg) {
        System.out.println(PREFIX + msg);
    }

    private static void error(String msg) {
        System.err.println(PREFIX + msg);
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
