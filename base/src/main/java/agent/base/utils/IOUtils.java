package agent.base.utils;

import java.io.*;
import java.net.Socket;

public class IOUtils {
    private static final int BUF_SIZE = 4096;

    public static String readToString(String filePath) throws IOException {
        return readToString(new FileInputStream(filePath));
    }

    public static String readToString(InputStream inputStream) throws IOException {
        return new String(readBytes(inputStream));
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(inputStream);
             ByteArrayOutputStream out = new ByteArrayOutputStream(BUF_SIZE)) {
            byte[] bs = new byte[BUF_SIZE];
            int offset;
            while ((offset = in.read(bs)) > -1) {
                out.write(bs, 0, offset);
            }
            return out.toByteArray();
        }
    }

    public static byte[] readBytes(String filePath) throws IOException {
        return readBytes(new File(filePath));
    }

    public static byte[] readBytes(File file) throws IOException {
        return readBytes(new FileInputStream(file));
    }

    public static void writeString(String filePath, String content, boolean append) throws IOException {
        try (Writer writer = new BufferedWriter(new FileWriter(filePath, append))) {
            writeString(writer, content);
        }
    }

    public static void writeString(Writer writer, String content) throws IOException {
        writer.write(content);
    }

    public static void writeBytes(String filePath, byte[] bs, boolean append) throws IOException {
        writeBytes(new File(filePath), bs, append);
    }

    public static void writeBytes(File file, byte[] bs, boolean append) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(file, append)) {
            writeBytes(outputStream, bs);
        }
    }

    public static void writeBytes(OutputStream outputStream, byte[] bs) throws IOException {
        outputStream.write(bs);
    }

    public static void write(String filePath, boolean append, BufferedWriteFunc writeFunc) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            writeFunc.exec(writer);
        }
    }

    public static void writeToConsole(BufferedWriteFunc writeFunc) throws Exception {
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(System.out)
        );
        writeFunc.exec(writer);
        writer.flush();
    }

    public static void read(String filePath, BufferedReadFunc func) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            func.exec(reader);
        }
    }

    public static void read(InputStream in, byte[] b) throws IOException {
        read(in, b, b.length);
    }

    public static void read(InputStream in, byte[] b, int size) throws IOException {
        int offset = 0;
        int off;
        int len = size;
        while ((off = in.read(b, offset, len)) > -1 && len > 0) {
            if (off > 0) {
                offset += off;
                len -= off;
            } else
                Utils.sleep(100);
        }
    }

    public static void close(Closeable o) {
        if (o != null) {
            try {
                o.close();
            } catch (IOException e) {
            }
        }
    }

    public static void close(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public interface BufferedWriteFunc {
        void exec(BufferedWriter writer) throws Exception;
    }

    public interface BufferedReadFunc {
        void exec(BufferedReader reader) throws Exception;
    }
}
