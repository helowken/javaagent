package test.server.log;

import agent.base.utils.FileUtils;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.FlushLogEvent;
import agent.server.event.impl.ResetEvent;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class BinaryLogWriterTest {
    //    @Test
    public void test() throws Exception {
        List<Long> usedTimes = new ArrayList<>();
        for (int i = 0; i < 20; ++i) {
            usedTimes.add(doTest());
        }
        System.out.println("======================");
        usedTimes.forEach(System.out::println);
    }


    private long doTest() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        File tmpDir = Files.createTempDirectory("test-binary-log").toFile();
        File logFile = new File(tmpDir, "test.log");
        configMap.put("outputPath", logFile.getAbsolutePath());
        configMap.put("autoFlush", false);
        configMap.put("maxBufferSize", 81920);
        configMap.put("bufferCount", 1000);
        configMap.put("writeTimeoutMs", 5);
        String logKey = LogMgr.regBinary(
                Collections.singletonMap("log", configMap),
                Collections.emptyMap()
        );

        final int count = 100;
        final int times = 10000;
        final int funcCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(count);
        try {
            long st = System.nanoTime();
            for (int i = 0; i < count; ++i) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < times; ++j) {
                            BinaryLogItem logItem = BinaryLogItemPool.get(logKey);
                            logItem.markAndPosition(Integer.BYTES);
                            for (int k = 0; k < funcCount; ++k) {
                                logItem.putInt(k);
                                logItem.putInt(k);
                            }
                            logItem.putIntToMark(funcCount);
//                            try {
//                                Thread.sleep(1);
//                            } catch (InterruptedException e) {
//                            }
                            LogMgr.logBinary(logKey, logItem);
                        }
                        endLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            startLatch.countDown();
            endLatch.await();
            long et = System.nanoTime();
            EventListenerMgr.fireEvent(new FlushLogEvent(logFile.getAbsolutePath()));
            EventListenerMgr.fireEvent(new ResetEvent(true));
            return et - st;
        } finally {
            FileUtils.removeFileOrDir(tmpDir);
        }
    }
}
