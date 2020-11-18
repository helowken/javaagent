package agent.builtin.tools.result;

import agent.base.utils.*;
import agent.base.utils.InvokeDescriptorUtils.TextConfig;
import agent.builtin.tools.result.parse.ResultParams;
import agent.common.struct.impl.Struct;
import agent.common.utils.MetadataUtils;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
abstract class AbstractResultHandler<T, P extends ResultParams> implements ResultHandler<P> {
    private static final Logger logger = Logger.getLogger(AbstractResultHandler.class);
    private static final int BUF_INIT_SIZE = 256 * 1024;
    private final ThreadLocal<byte[]> bufLocal = new ThreadLocal<>();

    abstract T calculate(Collection<File> dataFiles, P params);

    List<File> findDataFiles(String dataFilePath) {
        File dir = new File(dataFilePath).getParentFile();
        List<File> rsList = null;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null)
                rsList = Stream.of(files)
                        .filter(
                                file -> filterDataFile(file, dataFilePath)
                        )
                        .collect(Collectors.toList());
        }
        if (rsList == null || rsList.isEmpty())
            throw new RuntimeException("No data files found for: " + dataFilePath);
        return rsList;
    }

    private boolean filterDataFile(File file, String dataFilePath) {
        String filePath = file.getAbsolutePath();
        if (filePath.equals(dataFilePath))
            return true;
        int pos = filePath.lastIndexOf(".");
        String tmpPath = filePath;
        if (pos > -1)
            tmpPath = filePath.substring(0, pos);
        return tmpPath.equals(dataFilePath) && acceptFile(filePath);
    }

    protected boolean acceptFile(String filePath) {
        return !MetadataUtils.isMetadataFile(filePath);
    }

    T calculateStats(List<File> dataFiles, P params) {
        return TimeMeasureUtils.run(
                () -> {
                    ForkJoinPool pool = new ForkJoinPool(
                            Math.max(1,
                                    Runtime.getRuntime().availableProcessors() - 1
                            )
                    );
                    try {
                        return pool.submit(
                                () -> calculate(dataFiles, params)
                        ).get();
                    } finally {
                        pool.shutdown();
                    }
                },
                "Result calculation used time: {}"
        );
    }

    <T> T readMetadata(String inputPath) throws IOException {
        byte[] bs = IOUtils.readBytes(
                FileUtils.getValidFile(
                        MetadataUtils.getMetadataFile(inputPath)
                ).getAbsolutePath()
        );
        return (T) Struct.deserialize(
                ByteBuffer.wrap(bs)
        );
    }

    Map<Integer, InvokeMetadata> readInvokeMetadata(String inputPath) {
        try {
            Map<Integer, String> idToClassInvoke = readMetadata(inputPath);
            Map<Integer, InvokeMetadata> rsMap = new HashMap<>();
            idToClassInvoke.forEach(
                    (id, classInvoke) -> rsMap.put(
                            id,
                            DestInvokeIdRegistry.parse(classInvoke)
                    )
            );
            return rsMap;
        } catch (Exception e) {
            logger.error("Read metadata failed.", e);
            return Collections.emptyMap();
        }
    }

    String formatInvoke(InvokeMetadata metadata) {
        if (metadata.isUnknown())
            return metadata.invoke;
        TextConfig config = new TextConfig();
        config.withReturnType = false;
        config.withPkg = false;
        return InvokeDescriptorUtils.descToText(metadata.invoke, config);
    }

    String formatClassName(InvokeMetadata metadata) {
        if (metadata.isUnknown())
            return metadata.clazz;
        String result = InvokeDescriptorUtils.getSimpleName(metadata.clazz);
        if (metadata.idx > 1)
            result += "#" + metadata.idx + "";
        return result;
    }

    void calculateBinaryFile(File dataFile, CalculateBytesFunc calculateFunc) {
        calculateFile(
                dataFile,
                inputFile -> {
                    long length = inputFile.length();
                    try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)))) {
                        while (length > 0) {
                            length -= calculateFunc.exec(in);
                            if (length < 0)
                                throw new RuntimeException("Invalid calculation.");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Calculate data file failed: " + inputFile, e);
                    }
                }
        );
    }

    private byte[] getBuf(int destSize) {
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

    int deserializeBytes(DataInputStream in, Consumer<ByteBuffer> consumer) throws IOException {
        int totalSize = 0;
        int size = in.readInt();
        byte[] bs = getBuf(size);
        IOUtils.read(in, bs, size);
        totalSize += Integer.BYTES;
        totalSize += size;
        consumer.accept(
                ByteBuffer.wrap(bs, 0, size)
        );
        return totalSize;
    }

    void calculateTextFile(File dataFile, CalculateTextFunc calculateTextFunc) {
        calculateFile(
                dataFile,
                inputFile -> {
                    try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                        calculateTextFunc.exec(reader);
                    }
                }
        );
    }

    InvokeMetadata getMetadata(Map<Integer, InvokeMetadata> idToInvoke, Integer invokeId) {
        InvokeMetadata invokeMetadata = idToInvoke.get(invokeId);
        if (invokeMetadata == null) {
            logger.error("No metadata found for invoke id: {}", invokeId);
            invokeMetadata = InvokeMetadata.unknown(invokeId);
        }
        return invokeMetadata;
    }

    String convertInvoke(Integer parentInvokeId, Map<Integer, InvokeMetadata> idToInvoke, InvokeMetadata metadata) {
        String invoke = formatInvoke(metadata);
        String className = null;
        if (parentInvokeId == null)
            className = formatClassName(metadata);
        else {
            InvokeMetadata parentMetadata = getMetadata(idToInvoke, parentInvokeId);
            if (!parentMetadata.clazz.equals(metadata.clazz) ||
                    parentMetadata.idx != metadata.idx)
                className = formatClassName(metadata);
        }
        return Utils.isNotBlank(className) ?
                className + " # " + invoke :
                invoke;
    }

    private void calculateFile(File dataFile, ProcessFileFunc processFileFunc) {
        String path = dataFile.getAbsolutePath();
        TimeMeasureUtils.run(
                () -> processFileFunc.process(dataFile),
                e -> System.err.println("Read data file failed: " + path + '\n' + Utils.getErrorStackStrace(e)),
                "Calculate {} used time: {}",
                path
        );
    }

    private interface ProcessFileFunc {
        void process(File dataFilePath) throws Exception;
    }

    interface CalculateBytesFunc {
        int exec(DataInputStream in) throws Exception;
    }

    interface CalculateTextFunc {
        void exec(BufferedReader reader) throws Exception;
    }

}


