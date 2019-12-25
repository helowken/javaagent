package agent.builtin.tools.result;

import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.builtin.tools.CostTimeStatItem;
import agent.common.utils.JSONUtils;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.tree.Node;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractResultHandler<T> implements ResultHandler {

    @Override
    public void printResult(String outputPath, boolean skipAvgEq0, Set<Float> rates) throws Exception {
        printTree(
                readMetadata(outputPath),
                calculateStats(outputPath),
                skipAvgEq0,
                rates
        );
    }

    private T calculateStats(String outputPath) throws Exception {
        long st = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 1);
        try {
            return pool.submit(
                    () -> calculate(
                            findDataFiles(outputPath)
                    )
            ).get();
        } finally {
            long et = System.currentTimeMillis();
            System.out.println("Result calculation used time: " + (et - st) + "ms");
            pool.shutdown();
        }
    }

    private Map<String, Map<String, Integer>> readMetadata(String outputPath) throws IOException {
        return JSONUtils.read(
                IOUtils.readToString(
                        outputPath + DestInvokeIdRegistry.METADATA_FILE
                )
        );
    }

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

    void calculateFile(String dataFilePath, CalculateFunc calculateFunc) {
        try {
            long st = System.currentTimeMillis();
            File outputFile = new File(dataFilePath);
            long length = outputFile.length();
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFile)))) {
                while (length > 0) {
                    int totalSize = 0;
                    int count = in.readInt();
                    totalSize += Integer.BYTES;
                    for (int i = 0; i < count; ++i) {
                        int parentInvokeId = in.readInt();
                        int invokeId = in.readInt();
                        int costTime = in.readInt();
                        boolean error = in.readByte() == 1;
                        calculateFunc.exec(parentInvokeId, invokeId, costTime, error);
                        totalSize += Integer.BYTES * 3 + Byte.BYTES;
                    }
                    length -= totalSize;
                    if (length < 0)
                        throw new RuntimeException("Invalid calculation.");
                }
            }
            long et = System.currentTimeMillis();
            System.err.println("Calculate " + dataFilePath + " used time: " + (et - st) + "ms");
        } catch (IOException e) {
            System.err.println("Read data file failed: " + dataFilePath + "\n" + Utils.getErrorStackStrace(e));
        }
    }

    Node<String> newMethodNode(String destInvoke, CostTimeStatItem item, Set<Float> rates) {
        Node<String> invokeNode = new Node<>(
                formatInvoke(destInvoke)
        );
        invokeNode.appendChild(
                new Node<>(
                        item.getAvgTimeString()
                )
        );
        invokeNode.appendChild(
                new Node<>(
                        item.getMaxTimeString()
                )
        );
        invokeNode.appendChild(
                new Node<>(
                        item.getCountString()
                )
        );
        invokeNode.appendChild(
                new Node<>(
                        item.getTimeDistributionString(rates)
                )
        );
        return invokeNode;
    }

    private String formatInvoke(String method) {
        return method;
    }

    abstract T calculate(Collection<String> dataFiles);

    abstract void printTree(Map<String, Map<String, Integer>> classToInvokeToId, T result, boolean skipAvgEq0, Set<Float> rates);

    interface CalculateFunc {
        void exec(int parentInvokeId, int invokeId, int costTime, boolean error);
    }
}
