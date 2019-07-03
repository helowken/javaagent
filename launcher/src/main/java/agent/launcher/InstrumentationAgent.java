package agent.launcher;

import agent.base.utils.Logger;
import agent.hock.utils.AgentConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class InstrumentationAgent {
    private static final Logger logger = Logger.getLogger(InstrumentationAgent.class);
    private static final String AGENT_RUNNER_CLASS = "agent.server.AgentRunner";

    public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
        logger.info("In agentmain method: {}", agentArgs);
        AgentConfig config = AgentConfig.parse(agentArgs);
        initLog(config.logPath);
        ClassLoader loader = initClassLoader(config.libPath);
        startRunner(loader, instrumentation, config);
    }

    private static void initLog(String outputPath) {
        try {
            logger.info("Agent log file: {}", outputPath);
            if (outputPath != null)
                Logger.setOutputFile(outputPath);
        } catch (IOException e) {
            throw new RuntimeException("Init log failed.", e);
        }
    }

    private static void startRunner(ClassLoader loader, Instrumentation instrumentation, AgentConfig config) throws Exception {
        logger.info("Start agent runner.");
        Class<?> clazz = loader.loadClass(AGENT_RUNNER_CLASS);
        Method method = clazz.getDeclaredMethod("run", Instrumentation.class, AgentConfig.class);
        method.invoke(null, instrumentation, config);
    }


    private static ClassLoader initClassLoader(String libPath) throws Exception {
        ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = new URLClassLoader(
                collectJarUrls(libPath).toArray(new URL[0]),
                parentLoader
        );
        Thread.currentThread().setContextClassLoader(loader);
        return loader;
    }

    private static List<URL> collectJarUrls(String libPath) throws Exception {
        File libFile = new File(libPath);
        if (!libFile.exists())
            throw new FileNotFoundException("Lib path not exists: " + libPath);
        List<File> allFiles = new ArrayList<>();
        collectFiles(allFiles, libFile);
        if (allFiles.isEmpty())
            throw new RuntimeException("No jar file found in lib path: " + libPath);
        List<URL> urlList = new ArrayList<>();
        for (File file : allFiles) {
            urlList.add(file.toURI().toURL());
        }
        urlList.forEach(url -> logger.debug("Jar url: {}", url));
        return urlList;
    }

    private static void collectFiles(List<File> allFiles, File file) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    collectFiles(allFiles, subFile);
                }
            }
        } else
            allFiles.add(file);
    }
}
