package agent.jvmti;


import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JvmtiUtils {
    private static final Logger logger = Logger.getLogger(JvmtiUtils.class);
    private static final JvmtiUtils instance = new JvmtiUtils();

    public static JvmtiUtils getInstance() {
        return instance;
    }

    private JvmtiUtils() {
    }

    public void loadSelfLibrary() {
        String libName = getClass().getName().replaceAll("\\.", "_");
        System.loadLibrary(libName);
    }

    public void load(List<File> fileList) {
        load(fileList.stream()
                .map(File::getAbsolutePath)
                .toArray(String[]::new)
        );
    }

    public void load(String... libPaths) {
        if (libPaths != null) {
            for (String libPath : libPaths) {
                logger.debug("Load library on path into system: {}", libPath);
                try {
                    System.load(libPath);
                } catch (RuntimeException e) {
                    logger.error("Load library on path failed: {}", libPath);
                }
            }
        }
    }

    public <T> T findObjectByClassName(String className) throws Exception {
        List<T> rsList = findObjectsByClassName(className, 1);
        return rsList.isEmpty() ? null : rsList.get(0);
    }

    public <T> List<T> findObjectsByClassName(String className, int maxCount) throws Exception {
        return findObjectsByClass(
                ReflectionUtils.findClass(className),
                maxCount
        );
    }

    public <T> T findObjectByClass(Class<T> clazz) {
        List<T> rsList = findObjectsByClass(clazz, 1);
        return rsList.isEmpty() ? null : rsList.get(0);
    }

    public <T> List<T> findObjectsByClass(Class<T> clazz, int maxCount) {
        return Optional.ofNullable(
                findObjectsByClassHelper(clazz, maxCount)
        ).orElse(Collections.emptyList());
    }

    public boolean changeCredentialToTargetProcess(int pid) {
        return changeCredential(pid);
    }

    public boolean resetCredentialToSelfProcess() {
        return resetCredential();
    }

    public int getPid() {
        return getProcId();
    }

    public List<Class<?>> findLoadedClassList() {
        return getLoadedClasses();
    }

    private native <T> List<T> findObjectsByClassHelper(Class<T> clazz, int maxCount);

    private native List<Class<?>> getLoadedClasses();

    private native boolean changeCredential(int pid);

    private native boolean resetCredential();

    private native int getProcId();
}
