package test.server.tools;

import agent.base.utils.IOUtils;
import agent.base.utils.IndentUtils;
import agent.server.transform.impl.user.utils.CostTimeLogger;
import agent.server.utils.JSONUtils;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CostTimeStatisticsAnalyzeTest {
    @Test
    public void test() throws Exception {
        String outputPath = "/home/helowken/cost-time/cost-time-statistics.log";
        String metadataPath = outputPath + CostTimeLogger.METADATA_FILE;
        Map<String, Map<String, Map<String, Integer>>> contextToClassToMethodToType = readMetadata(metadataPath);

        Map<Integer, TimeItem> typeToTimeItem = new HashMap<>();
        File outputFile = new File(outputPath);
        long length = outputFile.length();
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFile)))) {
            while (length > 0) {
                int entrySize = in.readInt();
                for (int i = 0; i < entrySize; ++i) {
                    int methodType = in.readInt();
                    int costTime = in.readInt();
                    TimeItem item = typeToTimeItem.computeIfAbsent(methodType, key -> new TimeItem());
                    item.totalTime += costTime;
                    item.count += 1;
                }
                length -= Integer.BYTES + Integer.BYTES * 2 * entrySize;
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
                        System.out.println(IndentUtils.getIndent(2) + "method " + method + ": \t" + avgTime + "ms");
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
    }
}
