package agent.server.transform.revision;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;

import java.io.File;
import java.nio.file.Files;

public class ClassDataStore {
    public static final int REVISION_0 = 0;
    private static final Logger logger = Logger.getLogger(ClassDataStore.class);
    private static final String TMP_DIR_PREFIX = "javaagent";
    private static final String STORE_DIR = "class_data";
    private static File storeDir;

    private static synchronized File getStoreDir() {
        if (storeDir == null) {
            File tmpDir = Utils.wrapToRtError(
                    () -> Files.createTempDirectory(TMP_DIR_PREFIX).toFile()
            );
            storeDir = new File(tmpDir, STORE_DIR);
            if (!storeDir.mkdirs() && !storeDir.exists())
                throw new RuntimeException("Couldn't create store dir.");
        }
        return storeDir;
    }

    private static File getFile(Class<?> clazz, int revisionNum) {
        String relativePath = clazz.getName().replaceAll("\\.", File.separator) + "_" + System.identityHashCode(clazz) + "." + revisionNum;
        return new File(getStoreDir(), relativePath);
    }

    static void save(Class<?> clazz, byte[] data, int revisionNum) {
        File file = getFile(clazz, revisionNum);
        file.getParentFile().mkdirs();
        logger.debug("Save class {} [loader={}] data to: {}", clazz.getName(), clazz.getClassLoader(), file.getAbsolutePath());
        Utils.wrapToRtError(
                () -> IOUtils.writeBytes(file, data, false)
        );
    }

    public static byte[] load(Class<?> clazz, int revisionNum) {
        File file = getFile(clazz, revisionNum);
        logger.debug("Load class {} [loader={}] data from: {}", clazz.getName(), clazz.getClassLoader(), file.getAbsolutePath());
        return Utils.wrapToRtError(
                () -> IOUtils.readBytes(file)
        );
    }
}
