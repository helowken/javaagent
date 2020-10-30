package test;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class RedisClientTest {
    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final String CRLF = "\r\n";
    private static final boolean debugEnable = false;

    public static void main(String[] args) throws Exception {
        if (true) {
            test();
            return;
        }
        if (args.length < 3) {
            System.err.println("Usage: host port cmd");
            System.exit(-1);
        }
        access(
                args[0],
                Integer.parseInt(args[1]),
                args[2]
        );
    }

    private static void test() throws Exception {
        access("localhost", 6379, "LLEN mylist");
        System.out.println("--------------------------------");
        access("::1", 26379, "sentinel get-master-addr-by-name mymaster");
        System.out.println("--------------------------------");
        access("::1", 26380, "sentinel sentinels mymaster");
    }

    private static void access(String host, int port, String cmd) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port));
            OutputStream out = socket.getOutputStream();
            final DataInputStream in = new DataInputStream(socket.getInputStream());
            sendReq(out, cmd);
            receiveResp(in);
        }
    }

    private static void sendReq(OutputStream out, String cmd) throws Exception {
        StringTokenizer st = new StringTokenizer(cmd);
        List<String> parts = new ArrayList<>();
        String token;
        int count = 0;
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            parts.add("$" + token.length());
            parts.add(token);
            ++count;
        }
        parts.add(0, "*" + count);
        String s = join(CRLF, parts.toArray()) + CRLF;
        out.write(s.getBytes());
        out.flush();
    }

    private static void receiveResp(DataInputStream in) throws Exception {
        printResp(decode(in));
    }

    private static void printResp(Object v) {
        if (v instanceof Collection) {
            ((Collection) v).forEach(
                    o -> {
                        printResp(o);
                        if (o instanceof Collection)
                            System.out.println("==============");
                    }
            );
        } else
            System.out.println(v);
    }

    private static void debug(String msg) {
        if (debugEnable)
            System.out.println(msg);
    }

    private static Object decode(DataInputStream in) throws Exception {
        byte b = in.readByte();
        switch (b) {
            case '+':
                debug("Read string...");
                return readString(in);
            case '-':
                debug("Read error...");
                throw new RuntimeException("Error: " + readString(in));
            case ':':
                debug("Read integer...");
                return readInteger(in);
            case '$':
                debug("Read bulk string...");
                return readBulkString(in);
            case '*':
                debug("Read array...");
                return readArray(in);
            default:
                throw new Exception("Unknown byte: " + b);
        }
    }

    private static String readBulkString(DataInputStream in) throws Exception {
        int length = (int) readInteger(in);
        if (length == -1)
            return null;
        if (length == 0)
            return "";
        byte[] bs = new byte[length];
        int offset = 0;
        int restLen = length;
        while (restLen > 0) {
            int num = in.read(bs, offset, restLen);
            if (num == -1)
                throw new RuntimeException("Read bulk string failed.");
            else if (num == 0)
                Thread.sleep(10);
            offset += num;
            restLen -= num;
        }
        readCR(in);
        readLF(in);
        return new String(bs);
    }

    private static List<Object> readArray(DataInputStream in) throws Exception {
        long dimension = readInteger(in);
        if (dimension == -1)
            return null;
        if (dimension == 0)
            return Collections.emptyList();
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < dimension; ++i) {
            values.add(decode(in));
        }
        return values;
    }

    private static void readLF(DataInputStream in) throws Exception {
        byte b = in.readByte();
        if (b != LF)
            throw new RuntimeException("Expect LF read, but actually is: " + b);
    }

    private static void readCR(DataInputStream in) throws Exception {
        byte b = in.readByte();
        if (b != CR)
            throw new RuntimeException("Expect CR read, but actually is: " + b);
    }

    private static long readInteger(DataInputStream in) throws Exception {
        return Long.parseLong(
                readString(in)
        );
    }

    private static String readString(DataInputStream in) throws Exception {
        ByteArrayOutputStream bsOut = new ByteArrayOutputStream();
        while (true) {
            byte b = in.readByte();
            if (b == CR) {
                readLF(in);
                break;
            }
            bsOut.write(b);
        }
        return bsOut.toString();
    }

    public static <T> String join(String sep, T... vs) {
        if (vs == null)
            return null;
        if (vs.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vs.length; ++i) {
            if (i > 0)
                sb.append(sep);
            sb.append(vs[i]);
        }
        return sb.toString();
    }
}
