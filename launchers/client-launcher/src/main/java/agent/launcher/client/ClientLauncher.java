package agent.launcher.client;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.HostAndPort;
import agent.base.utils.LockObject;
import agent.base.utils.ProcessUtils;
import agent.cmdline.args.parse.ArgsOptsParser;
import agent.cmdline.args.parse.KeyValueOptParser;
import agent.cmdline.args.parse.Opts;
import agent.cmdline.args.parse.StoreOtherArgsOptParser;
import agent.jvmti.JvmtiUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static agent.base.utils.ProcessUtils.ProcConfig;
import static agent.base.utils.ProcessUtils.ProcessExecResult;

public class ClientLauncher {
    private static final ConsoleLogger logger = ConsoleLogger.getInstance();
    private static final long JOIN_TIMEOUT = 5000;
    private static final LockObject logLock = new LockObject();

    public static void main(String[] args) {
        try {
            JvmtiUtils.getInstance().loadSelfLibrary();
            List<String> cmdArgs = getCmdArgsForChildProc(args.length);
            StoreOtherArgsOptParser storeOtherArgsOptParser = new StoreOtherArgsOptParser();
            Opts opts = getOpts(cmdArgs, storeOtherArgsOptParser);
            Collection<HostAndPort> hostAndPorts = parseAddrs(opts);
            List<String> restArgs = storeOtherArgsOptParser.getArgs();

            int addrCount = hostAndPorts.size();
            if (addrCount == 1)
                ChildProcessLauncher.main(args);
            else {
                CountDownLatch startLatch = new CountDownLatch(1);
                CountDownLatch endLatch = new CountDownLatch(addrCount);
                List<Thread> ts = new ArrayList<>();
                for (HostAndPort hostAndPort : hostAndPorts) {
                    ts.add(
                            startChildThread(restArgs, hostAndPort, startLatch, endLatch)
                    );
                }
                startLatch.countDown();
                try {
                    endLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                joinThreads(ts);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            ConsoleLogger.getInstance().error("Error: {}", t.getMessage());
        }
    }

    private static void joinThreads(List<Thread> ts) {
        ts.forEach(
                t -> {
                    try {
                        t.join(JOIN_TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    private static Thread startChildThread(List<String> restArgs, HostAndPort hostAndPort, CountDownLatch startLatch, CountDownLatch endLatch) {
        String[] childArgs = getChildArgs(
                restArgs,
                hostAndPort
        );
        Thread t = new Thread(
                () -> {
                    try {
                        startLatch.await();
                        ProcessExecResult rs = ProcessUtils.exec(
                                new ProcConfig(childArgs)
                        );
                        logProcResult(rs, hostAndPort);
                    } catch (Exception e) {
                        logLock.sync(
                                lock -> logger.error("Start child process failed.", e)
                        );
                    } finally {
                        endLatch.countDown();
                    }
                }
        );
        t.start();
        return t;
    }

    private static void logProcResult(ProcessExecResult rs, HostAndPort hostAndPort) {
        logLock.sync(
                lock -> {
                    logger.error(
                            "\n========== {}: {}",
                            hostAndPort,
                            rs.isSuccess() ? "Success" : "Failed"
                    );
                    if (rs.hasOutputContent())
                        logger.error(
                                "{}",
                                rs.getOutputString()
                        );
                    if (rs.hasErrorContent())
                        logger.error(
                                "--------- Error:\n{}",
                                rs.getErrorString()
                        );
                }
        );
    }

    private static String[] getChildArgs(List<String> restArgs, HostAndPort hostAndPort) {
        List<String> childArgs = new ArrayList<>(restArgs);
        Collections.addAll(
                childArgs,
                AddressOptConfigs.OPT_ADDR,
                hostAndPort.toString()
        );
        return childArgs.toArray(new String[0]);
    }

    private static List<String> getCmdArgsForChildProc(int argLen) throws Exception {
        List<String> cmdArgs = getCmdArgList(
                JvmtiUtils.getInstance().getPid()
        );
        cmdArgs.set(
                cmdArgs.size() - argLen - 1,
                ChildProcessLauncher.class.getName()
        );
        return cmdArgs;
    }

    private static Opts getOpts(List<String> cmdArgs, StoreOtherArgsOptParser storeOtherArgsOptParser) {
        return new ArgsOptsParser(
                new KeyValueOptParser(
                        AddressOptConfigs.getSuite()
                ),
                storeOtherArgsOptParser
        ).parse(
                cmdArgs.toArray(new String[0])
        ).getOpts();
    }

    private static Collection<HostAndPort> parseAddrs(Opts opts) {
        Collection<HostAndPort> hostAndPorts = AddressUtils.parseAddrs(
                AddressOptConfigs.getAddress(opts)
        );
        if (hostAndPorts.isEmpty())
            throw new RuntimeException("No address specified.");
        return hostAndPorts;
    }

    private static List<String> getCmdArgList(int pid) throws Exception {
        List<String> argList = new ArrayList<>();
        String cmdlineFilePath = "/proc/" + pid + "/cmdline";
        StringBuilder sb = null;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(cmdlineFilePath))) {
            byte[] bs = new byte[512];
            int offset;
            while ((offset = in.read(bs, 0, bs.length)) > -1) {
                for (int i = 0; i < offset; ++i) {
                    if (bs[i] == 0) {
                        if (sb != null) {
                            argList.add(
                                    sb.toString()
                            );
                            sb = null;
                        }
                    } else {
                        if (sb == null)
                            sb = new StringBuilder();
                        sb.append((char) bs[i]);
                    }
                }
            }
        }
        return argList;
    }
}
