package agent.base.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ProcessUtils {
    private static final Logger logger = Logger.getLogger(ProcessUtils.class);
    private static final long DEFAULT_JOIN_DURATION_MS = 5000;

    public static String[] splitCmd(String cmd) {
        return Stream.of(cmd.split(" "))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    public static ProcessExecResult exec(String cmd) throws Exception {
        return exec(
                new ProcConfig(
                        splitCmd(cmd)
                )
        );
    }

    public static ProcessExecResult exec(ProcConfig procConfig) throws Exception {
        BufferedOutputConsumer outputConsumer = new BufferedOutputConsumer();
        BufferedOutputConsumer errorConsumer = new BufferedOutputConsumer();
        int exitValue = exec(procConfig, outputConsumer, errorConsumer);
        return new ProcessExecResult(outputConsumer.getContent(), errorConsumer.getContent(), exitValue);
    }

    public static int exec(ProcConfig procConfig, OutputConsumer outputConsumer, OutputConsumer errorConsumer) throws Exception {
        return exec(procConfig, outputConsumer, errorConsumer, DEFAULT_JOIN_DURATION_MS, TimeUnit.MILLISECONDS);
    }

    public static int exec(ProcConfig procConfig, OutputConsumer outputConsumer, OutputConsumer errorConsumer, long joinDuration, TimeUnit timeUnit) throws Exception {
        Process proc = Runtime.getRuntime().exec(procConfig.cmd, procConfig.envp, procConfig.dir);
        Thread outputThread = createAndStart(proc.getInputStream(), outputConsumer);
        Thread errorThread = createAndStart(proc.getErrorStream(), errorConsumer);
        int exitValue = proc.waitFor();
        waitJoin(Arrays.asList(outputThread, errorThread), joinDuration, timeUnit);
        return exitValue;
    }

    private static Thread createAndStart(InputStream inputStream, OutputConsumer outputConsumer) {
        Thread outputThread = new ProcessOutputThread(inputStream, outputConsumer);
        outputThread.start();
        return outputThread;
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

    private static class ProcessOutputThread extends Thread {
        private final InputStream inputStream;
        private final OutputConsumer outputConsumer;

        private ProcessOutputThread(InputStream inputStream, OutputConsumer outputConsumer) {
            this.inputStream = new BufferedInputStream(inputStream);
            this.outputConsumer = outputConsumer;
        }

        @Override
        public void run() {
            byte[] buff = new byte[1024];
            try {
                int offset;
                while ((offset = inputStream.read(buff)) > -1) {
                    outputConsumer.consume(buff, 0, offset);
                }
            } catch (Exception e) {
                logger.error("Read output failed.", e);
            } finally {
                IOUtils.close(this.inputStream);
            }
        }
    }

    public interface OutputConsumer {
        void consume(byte[] buff, int pos, int offset);
    }

    private static class BufferedOutputConsumer implements OutputConsumer {
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
        private final byte[] output;
        private final byte[] error;
        private final int exitValue;
        private volatile String outputString;
        private volatile String errorString;

        private ProcessExecResult(byte[] output, byte[] error, int exitValue) {
            this.output = Arrays.copyOf(output, output.length);
            this.error = Arrays.copyOf(error, error.length);
            this.exitValue = exitValue;
        }

        public boolean isSuccess() {
            return exitValue == 0;
        }

        public byte[] getOutput() {
            return output;
        }

        public byte[] getError() {
            return error;
        }

        public int getExitValue() {
            return exitValue;
        }

        public boolean hasOutputContent() {
            return output.length > 0;
        }

        public boolean hasErrorContent() {
            return error.length > 0;
        }

        public synchronized String getOutputString() {
            if (outputString == null)
                outputString = new String(output);
            return outputString;
        }

        public synchronized String getErrorString() {
            if (errorString == null)
                errorString = new String(error);
            return errorString;
        }
    }

    public static class ProcConfig {
        private final String[] cmd;
        private final String[] envp;
        private final File dir;

        public ProcConfig(String[] cmd) {
            this(cmd, null);
        }

        public ProcConfig(String[] cmd, String[] envp) {
            this(cmd, envp, null);
        }

        public ProcConfig(String[] cmd, String[] envp, File dir) {
            this.cmd = cmd;
            this.envp = envp;
            this.dir = dir;
        }
    }
}
