package agent.builtin.tools.result;

import agent.base.utils.*;
import agent.base.utils.InvokeDescriptorUtils.TextConfig;
import agent.builtin.tools.result.parse.ResultParams;
import agent.common.struct.impl.Struct;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractResultHandler<T, P extends ResultParams> implements ResultHandler<P> {
    AtomicReference<byte[]> bufRef = new AtomicReference<>(
            new byte[256 * 1024]
    );

    abstract T calculate(Collection<File> dataFiles, P params);

    List<File> findDataFiles(String dataFilePath) {
        File dir = new File(dataFilePath).getParentFile();
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null)
                return Stream.of(files)
                        .filter(
                                file -> filterDataFile(file, dataFilePath)
                        )
                        .collect(Collectors.toList());
        }
        return Collections.emptyList();
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
        return !DestInvokeIdRegistry.isMetadataFile(filePath);
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

    Map<Integer, InvokeMetadata> readMetadata(String inputPath) throws IOException {
        byte[] bs = IOUtils.readBytes(
                FileUtils.getValidFile(
                        DestInvokeIdRegistry.getMetadataFile(inputPath)
                ).getAbsolutePath()
        );
        Map<Integer, String> idToClassInvoke = Struct.deserialize(
                ByteBuffer.wrap(bs)
        );
        Map<Integer, InvokeMetadata> rsMap = new HashMap<>();
        idToClassInvoke.forEach(
                (id, classInvoke) -> rsMap.put(
                        id,
                        DestInvokeIdRegistry.parse(classInvoke)
                )
        );
        return rsMap;
    }

    String formatInvoke(String method) {
        TextConfig config = new TextConfig();
        config.withReturnType = false;
        config.withPkg = false;
        return InvokeDescriptorUtils.descToText(method, config);
    }

    String formatClassName(InvokeMetadata metadata) {
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
                    }
                }
        );
    }

    int deserializeBytes(DataInputStream in, Consumer<ByteBuffer> consumer) throws IOException {
        int totalSize = 0;
        int size = in.readInt();
        byte[] bs = bufRef.get();
        if (bs.length < size) {
            bs = new byte[bs.length * 2];
            bufRef.set(bs);
        }
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
        return Optional.ofNullable(
                idToInvoke.get(invokeId)
        ).orElseThrow(
                () -> new RuntimeException("No metadata found for invoke id: " + invokeId)
        );
    }

    String convertInvoke(Integer parentInvokeId, Map<Integer, InvokeMetadata> idToInvoke, InvokeMetadata metadata) {
        String invoke = formatInvoke(metadata.invoke);
        if (parentInvokeId == null)
            invoke = formatClassName(metadata) + " # " + invoke;
        else {
            InvokeMetadata parentMetadata = getMetadata(idToInvoke, parentInvokeId);
            if (!parentMetadata.clazz.equals(metadata.clazz) ||
                    parentMetadata.idx != metadata.idx)
                invoke = formatClassName(metadata) + " # " + invoke;
        }
        return invoke;
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


