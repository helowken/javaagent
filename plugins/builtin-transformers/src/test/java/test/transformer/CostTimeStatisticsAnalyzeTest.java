package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.IndentUtils;
import agent.builtin.transformer.utils.CostTimeLogger;
import agent.common.utils.JSONUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CostTimeStatisticsAnalyzeTest {
    public static void main(String[] args) throws Exception {
        String outputPath = "/home/helowken/cost-time/cost-time-statistics.log";
//        String outputPath = args[0];
        String metadataPath = outputPath + CostTimeLogger.METADATA_FILE;
        Map<String, Map<String, Map<String, Integer>>> contextToClassToMethodToType = readMetadata(metadataPath);

        Map<Integer, TimeItem> typeToTimeItem = new HashMap<>();
        File outputFile = new File(outputPath);
        long length = outputFile.length();
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFile)))) {
            while (length > 0) {
                int totalSize = in.readInt() * 2;
                for (int i = 0; i < totalSize; i += 2) {
                    int methodType = in.readInt();
                    int costTime = in.readInt();
                    TimeItem item = typeToTimeItem.computeIfAbsent(methodType, key -> new TimeItem());
                    item.totalTime += costTime;
                    item.count += 1;
                    if (costTime > item.maxTime)
                        item.maxTime = costTime;
                }
                length -= Integer.BYTES * (totalSize + 1);
            }
        }

        contextToClassToMethodToType.forEach((context, classToMethodToType) -> {
            System.out.println("===== Context: " + context);
            classToMethodToType.forEach((className, methodToType) -> {
                System.out.println(IndentUtils.getIndent(1) + "Class: " + className);
                new TreeMap<>(methodToType).forEach((method, type) -> {
                    TimeItem item = typeToTimeItem.get(type);
                    if (item != null) {
                        int avgTime = item.totalTime / item.count;
                        System.out.println(IndentUtils.getIndent(2) + "method " + method + ", count: " + item.count +
                                ", avg time: " + avgTime + "ms, max time: " + item.maxTime + "ms");
                    }
                });
            });
        });
    }

    private static Map<String, Map<String, Map<String, Integer>>> readMetadata(String metadataPath) throws IOException {
        String content = IOUtils.readToString(metadataPath);
        return JSONUtils.read(content);
    }

    private static class TimeItem {
        int totalTime = 0;
        int count = 0;
        int maxTime = 0;
    }
}
