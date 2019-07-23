package agent.base.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ProcessUtils {
    private static final Logger logger = Logger.getLogger(ProcessUtils.class);
    private static final long DEFAULT_JOIN_DURATION_MS = 5000;

    public static String getJvmPidByDisplayName(String displayName) throws Exception {
        ProcessExecResult result = exec("jps -l");
        if (result.isSuccess()) {
            logger.debug("Get jvm pid success, Input: \n{}", result.getInputString());
            return Stream.of(result.getInputString().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.split(" "))
                    .filter(ts -> ts.length == 2 && ts[1].contains(displayName))
                    .map(ts -> ts[0])
                    .findAny()
                    .orElse(null);
        }
        logger.error("Get jvm pid failed, exit value: {}\nInput: \n{}\n\nError:\n{}", result.getExitValue(), result.getInputString(), result.getErrorString());
        return null;
    }

    public static ProcessExecResult exec(String cmd) throws Exception {
        return exec(
                Stream.of(cmd.split(" "))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new)
        );
    }

    public static ProcessExecResult exec(String[] cmd) throws Exception {
        BufferedInputConsumer inputConsumer = new BufferedInputConsumer();
        BufferedInputConsumer errorConsumer = new BufferedInputConsumer();
        int exitValue = exec(cmd, inputConsumer, errorConsumer);
        return new ProcessExecResult(inputConsumer.getContent(), errorConsumer.getContent(), exitValue);
    }

    public static int exec(String[] cmd, InputConsumer inputConsumer, InputConsumer errorConsumer) throws Exception {
        return exec(cmd, inputConsumer, errorConsumer, DEFAULT_JOIN_DURATION_MS, TimeUnit.MILLISECONDS);
    }

    public static int exec(String[] cmd, InputConsumer inputConsumer, InputConsumer errorConsumer, long joinDuration, TimeUnit timeUnit) throws Exception {
        Process proc = Runtime.getRuntime().exec(cmd);
        Thread inputThread = createAndStart(proc.getInputStream(), inputConsumer);
        Thread errorThread = createAndStart(proc.getErrorStream(), errorConsumer);
        int exitValue = proc.waitFor();
        waitJoin(Arrays.asList(inputThread, errorThread), joinDuration, timeUnit);
        return exitValue;
    }

    private static Thread createAndStart(InputStream inputStream, InputConsumer inputConsumer) {
        Thread inputThread = new ProcessInputThread(inputStream, inputConsumer);
        inputThread.setDaemon(true);
        inputThread.start();
        return inputThread;
    }

    private static void waitJoin(List<Thread> threadList, long duration, TimeUnit timeUnit) {
        List<Thread> joinThreadList = new ArrayList<>(threadList);
        long restTime = timeUnit.toMillis(duration);
        long startTime = System.currentTimeMillis();
        while (!joinThreadList.isEmpty()) {
            try {
                joinThreadList.remove(0).join(restTime);
            } catch (InterruptedException e) {
                return;
            }
            long endTime = System.currentTimeMillis();
            restTime -= (endTime - startTime);
            if (restTime <= 0)
                return;
            startTime = endTime;
        }
    }

    private static class ProcessInputThread extends Thread {
        private final InputStream inputStream;
        private final InputConsumer inputConsumer;

        private ProcessInputThread(InputStream inputStream, InputConsumer inputConsumer) {
            this.inputStream = new BufferedInputStream(inputStream);
            this.inputConsumer = inputConsumer;
        }

        @Override
        public void run() {
            byte[] buff = new byte[1024];
            try {
                int offset;
                while ((offset = inputStream.read(buff)) > -1) {
                    inputConsumer.consume(buff, 0, offset);
                }
            } catch (Exception e) {
                logger.error("Read input failed.", e);
            } finally {
                IOUtils.close(this.inputStream);
            }
        }
    }

    public interface InputConsumer {
        void consume(byte[] buff, int pos, int offset);
    }

    private static class BufferedInputConsumer implements InputConsumer {
        private ByteArrayOutputStream out = new ByteArrayOutputStream(1024);

        @Override
        public void consume(byte[] buff, int pos, int offset) {
            out.write(buff, pos, offset);
        }

        public byte[] getContent() {
            return out.toByteArray();
        }
    }

    public static class ProcessExecResult {
        private final byte[] input;
        private final byte[] error;
        private final int exitValue;
        private volatile String inputString;
        private volatile String errorString;
        private final LockObject rsLock = new LockObject();

        private ProcessExecResult(byte[] input, byte[] error, int exitValue) {
            this.input = input;
            this.error = error;
            this.exitValue = exitValue;
        }

        public boolean isSuccess() {
            return exitValue == 0;
        }

        public byte[] getInput() {
            return input;
        }

        public byte[] getError() {
            return error;
        }

        public int getExitValue() {
            return exitValue;
        }

        public String getInputString() {
            if (inputString == null)
                rsLock.sync(lock -> {
                    if (inputString == null)
                        inputString = new String(input);
                });
            return inputString;
        }

        public String getErrorString() {
            if (errorString == null)
                rsLock.sync(lock -> {
                    if (errorString == null)
                        errorString = new String(input);
                });
            return errorString;
        }
    }
}
