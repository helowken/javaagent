package agent.common.network;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.common.buffer.BufferAllocator;
import agent.common.buffer.ByteUtils;
import agent.common.message.Message;
import agent.common.network.exception.ConnectionClosedException;
import agent.common.struct.DefaultBBuff;

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

    public ByteBuffer receive() throws Exception {
        int size = in.readInt();
        logger.debug("Read message bytesSize: {}", size);
        if (size == -1)
            throw new ConnectionClosedException("Connection closed.");
        byte[] b = new byte[size];
        int offset = 0;
        int off;
        int len = size;
        while ((off = in.read(b, offset, len)) > -1 && len > 0) {
            if (off > 0) {
                offset += off;
                len -= off;
            } else {
                logger.debug("sleep to wait more data.");
                Utils.sleep(100);
            }
        }
        logger.debug("Rest length: {}", len);
        return ByteBuffer.wrap(b);
    }

    public void send(Message message) throws Exception {
        ByteBuffer bb = BufferAllocator.allocate(message.bytesSize());
        message.serialize(
                new DefaultBBuff(bb)
        );
        byte[] bs = ByteUtils.getBytes(bb);
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
    }
}
