package agent.common.network;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.common.buffer.ByteUtils;
import agent.common.message.Message;
import agent.common.message.MessageMgr;
import agent.common.network.exception.ConnectionClosedException;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageIO implements Closeable {
    private static final Logger logger = Logger.getLogger(MessageIO.class);
    private static final List<Class<? extends Exception>> networkErrorClassList = Collections.unmodifiableList(Arrays.asList(
            SocketException.class,
            ConnectException.class,
            SocketTimeoutException.class,
            UnknownHostException.class,
            NoRouteToHostException.class,
            EOFException.class
    ));
    private DataInputStream in;
    private DataOutputStream out;

    public static MessageIO create(Socket socket) throws IOException {
        return new MessageIO(socket.getInputStream(), socket.getOutputStream());
    }

    private MessageIO(InputStream in, OutputStream out) {
        this.in = new DataInputStream(in);
        this.out = new DataOutputStream(out);
    }

    public Message receive() throws Exception {
        int size = in.readInt();
        if (size == -1)
            throw new ConnectionClosedException("Connection closed.");
        logger.debug("Read message bytesSize: {}", size);
        byte[] b = new byte[size];
        IOUtils.read(in, b, size);
        return MessageMgr.deserialize(
                ByteBuffer.wrap(b)
        );
    }

    public void send(Message message) throws Exception {
        byte[] bs = ByteUtils.getBytes(
                MessageMgr.serialize(message)
        );
        out.writeInt(bs.length);
        out.write(bs);
        out.flush();
        logger.debug("Write message bytesSize: {}", bs.length);
    }

    public static boolean isNetworkException(Throwable e) {
        for (Class<? extends Exception> errorClass : networkErrorClassList) {
            if (errorClass.isInstance(e))
                return true;
        }
        Throwable cause = e.getCause();
        return cause != null && isNetworkException(cause);
    }

    public void close() {
        IOUtils.close(in);
        IOUtils.close(out);
        in = null;
        out = null;
    }
}
