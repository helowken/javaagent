package agent.base.utils;

import java.io.*;

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

    public static void close(Closeable o) {
        if (o != null) {
            try {
                o.close();
            } catch (IOException e) {
            }
        }
    }
}
