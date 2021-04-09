package agent.builtin.tools.execute;

import agent.base.utils.*;
import agent.common.utils.MetadataUtils;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResultExecUtils {
    private static final Logger logger = Logger.getLogger(ResultExecUtils.class);
    private static final int BUF_INIT_SIZE = 256 * 1024;
    private static final ThreadLocal<byte[]> bufLocal = new ThreadLocal<>();

    public static List<File> findDataFiles(String dataFilePath, Predicate<String> fileFilter) {
        File dir = new File(dataFilePath).getParentFile();
        List<File> rsList = null;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null)
                rsList = Stream.of(files)
                        .filter(
                                file -> filterDataFile(file, dataFilePath, fileFilter)
                        )
                        .collect(Collectors.toList());
        }
        if (rsList == null || rsList.isEmpty())
            throw new RuntimeException("No data files found for: " + dataFilePath);
        return rsList;
    }

    private static boolean filterDataFile(File file, String dataFilePath, Predicate<String> fileFilter) {
        String filePath = file.getAbsolutePath();
        if (filePath.equals(dataFilePath))
            return true;
        int pos = filePath.lastIndexOf(".");
        String tmpPath = filePath;
        if (pos > -1)
            tmpPath = filePath.substring(0, pos);
        return tmpPath.equals(dataFilePath) && fileFilter.test(filePath);
    }

    public static <T> T calculateAll(Callable<T> callable) {
        return TimeMeasureUtils.run(
                () -> {
                    ForkJoinPool pool = new ForkJoinPool(
                            Math.max(1,
                                    Runtime.getRuntime().availableProcessors() - 1
                            )
                    );
                    try {
                        return pool.submit(callable).get();
                    } finally {
                        pool.shutdown();
                    }
                },
                "Result calculation used time: {}"
        );
    }

    public static Map<Integer, InvokeMetadata> readInvokeMetadata(String inputPath) {
        try {
            byte[] bs = IOUtils.readBytes(
                    FileUtils.getAbsolutePath(
                            MetadataUtils.getMetadataFile(inputPath),
                            true
                    )
            );
            return DestInvokeIdRegistry.getInstance().parse(bs);
        } catch (Exception e) {
            logger.error("Read metadata failed.", e);
            return Collections.emptyMap();
        }
    }

    public static String formatInvoke(InvokeMetadata metadata) {
        if (metadata.isUnknown())
            return metadata.invoke;
        InvokeDescriptorUtils.TextConfig config = new InvokeDescriptorUtils.TextConfig();
        config.withReturnType = false;
        config.withPkg = false;
        return InvokeDescriptorUtils.descToText(metadata.invoke, config);
    }

    public static String formatClassName(InvokeMetadata metadata) {
        if (metadata.isUnknown())
            return metadata.clazz;
        String className = InvokeDescriptorUtils.getSimpleName(metadata.clazz);
        return metadata.cid > 1 ?
                Utils.getClassNameWithId(className, metadata.cid) :
                className;
    }

    public static void calculateBinaryFile(File dataFile, CalculateBytesFunc calculateFunc) {
        String path = dataFile.getAbsolutePath();
        TimeMeasureUtils.run(
                () -> {
                    long length = dataFile.length();
                    try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(dataFile)))) {
                        while (length > 0) {
                            length -= calculateFunc.exec(in);
                            if (length < 0)
                                throw new RuntimeException("Invalid calculation.");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Calculate data file failed: " + dataFile, e);
                    }
                },
                e -> System.err.println("Read data file failed: " + path + '\n' + Utils.getErrorStackStrace(e)),
                "Calculate {} used time: {}",
                path
        );
    }

    private static byte[] getBuf(int destSize) {
        byte[] bs = bufLocal.get();
        int size = bs == null ? BUF_INIT_SIZE : bs.length;
        while (size < destSize) {
            size *= 2;
        }
        if (bs == null || size > bs.length) {
            bs = new byte[size];
            bufLocal.set(bs);
        }
        return bs;
    }

    public static int deserializeBytes(DataInput in, Consumer<ByteBuffer> consumer, boolean hasSizeAfter) throws IOException {
        int totalSize = 0;
        int size = in.readInt();
        byte[] bs = getBuf(size);
        in.readFully(bs, 0, size);
        totalSize += Integer.BYTES;
        totalSize += size;
        if (hasSizeAfter) {
            in.readInt();
            totalSize += Integer.BYTES;
        }
        consumer.accept(
                ByteBuffer.wrap(bs, 0, size)
        );
        return totalSize;
    }

    public static InvokeMetadata getMetadata(Map<Integer, InvokeMetadata> idToInvoke, Integer invokeId) {
        InvokeMetadata invokeMetadata = idToInvoke.get(invokeId);
        if (invokeMetadata == null) {
            logger.error("No metadata found for invoke id: {}", invokeId);
            invokeMetadata = InvokeMetadata.unknown(invokeId);
        }
        return invokeMetadata;
    }

    public static String convertInvoke(Integer parentInvokeId, Map<Integer, InvokeMetadata> idToInvoke, InvokeMetadata metadata) {
        String invoke = formatInvoke(metadata);
        String className = null;
        if (parentInvokeId == null)
            className = formatClassName(metadata);
        else {
            InvokeMetadata parentMetadata = getMetadata(idToInvoke, parentInvokeId);
            if (!parentMetadata.clazz.equals(metadata.clazz) ||
                    parentMetadata.cid != metadata.cid)
                className = formatClassName(metadata);
        }
        return Utils.isNotBlank(className) ?
                className + " # " + invoke :
                invoke;
    }


    public interface CalculateBytesFunc {
        int exec(DataInputStream in) throws Exception;
    }
}
