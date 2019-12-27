package agent.builtin.tools.result;

import agent.base.utils.IOUtils;
import agent.base.utils.MethodDescriptorUtils;
import agent.base.utils.Utils;
import agent.common.utils.JSONUtils;
import agent.server.transform.impl.DestInvokeIdRegistry;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractResultHandler<T> {
    private List<String> findDataFiles(String dataFilePath) {
        File dir = new File(dataFilePath).getParentFile();
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null) {
                return Stream.of(files)
                        .map(File::getAbsolutePath)
                        .filter(filePath -> {
                            if (filePath.equals(dataFilePath))
                                return true;
                            int pos = filePath.lastIndexOf(".");
                            String tmpPath = filePath;
                            if (pos > -1)
                                tmpPath = filePath.substring(0, pos);
                            return tmpPath.equals(dataFilePath) && !filePath.endsWith(DestInvokeIdRegistry.METADATA_FILE);
                        })
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    T calculateStats(String inputPath) throws Exception {
        long st = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 1);
        try {
            return pool.submit(
                    () -> calculate(
                            findDataFiles(inputPath)
                    )
            ).get();
        } finally {
            long et = System.currentTimeMillis();
            System.out.println("Result calculation used time: " + (et - st) + "ms");
            pool.shutdown();
        }
    }

    Map<String, Map<String, Integer>> readMetadata(String inputPath) throws IOException {
        return JSONUtils.read(
                IOUtils.readToString(
                        inputPath + DestInvokeIdRegistry.METADATA_FILE
                )
        );
    }

    Map<Integer, InvokeMetadata> convertMetadata(Map<String, Map<String, Integer>> classToInvokeToId) {
        Map<Integer, InvokeMetadata> rsMap = new HashMap<>();
        classToInvokeToId.forEach(
                (clazz, invokeToId) -> invokeToId.forEach(
                        (invoke, id) -> rsMap.put(
                                id,
                                new InvokeMetadata(clazz, invoke)
                        )
                )
        );
        return rsMap;
    }

    String formatInvoke(String method) {
        return MethodDescriptorUtils.descToText(method, true);
    }

    void calculateBytesFile(String dataFilePath, CalculateBytesFunc calculateFunc) {
        calculateFile(
                dataFilePath,
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

    void calculateTextFile(String dataFilePath, CalculateTextFunc calculateTextFunc) {
        calculateFile(
                dataFilePath,
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
            invoke = metadata.clazz + "# " + invoke;
        else {
            InvokeMetadata parentMetadata = getMetadata(idToInvoke, parentInvokeId);
            if (!parentMetadata.clazz.equals(metadata.clazz))
                invoke = metadata.clazz + "# " + invoke;
        }
        return invoke;
    }

    private void calculateFile(String dataFilePath, ProcessFileFunc processFileFunc) {
        try {
            long st = System.currentTimeMillis();
            processFileFunc.process(
                    new File(dataFilePath)
            );
            long et = System.currentTimeMillis();
            System.out.println("Calculate " + dataFilePath + " used time: " + (et - st) + "ms");
        } catch (Exception e) {
            System.err.println("Read data file failed: " + dataFilePath + "\n" + Utils.getErrorStackStrace(e));
        }
    }

    abstract T calculate(Collection<String> dataFiles);

    private interface ProcessFileFunc {
        void process(File dataFilePath) throws Exception;
    }

    interface CalculateBytesFunc {
        int exec(DataInputStream in) throws Exception;
    }

    interface CalculateTextFunc {
        void exec(BufferedReader reader) throws Exception;
    }

    static class InvokeMetadata {
        final String clazz;
        final String invoke;

        InvokeMetadata(String clazz, String invoke) {
            this.clazz = clazz;
            this.invoke = invoke;
        }
    }
}
