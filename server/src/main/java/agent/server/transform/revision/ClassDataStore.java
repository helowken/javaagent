package agent.server.transform.revision;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.StringItem;

import java.io.File;

public class ClassDataStore {
    private static final Logger logger = Logger.getLogger(ClassDataStore.class);
    private final String dir;

    public ClassDataStore(String dir) {
        this.dir = dir;
        logger.debug("Class data store dir: {}", dir);
    }

    File getClassDataFile(Class<?> clazz) {
        String relativePath = new StringItem(clazz.getName())
                .replaceAll(".", File.separator)
                .replaceAll("$", "#") + "_" +
                System.identityHashCode(clazz.getClassLoader()) + "_" +
                System.identityHashCode(clazz) + ".class";
        return new File(dir, relativePath);
    }

    public void save(Class<?> clazz, byte[] data) throws Exception {
        File file = getClassDataFile(clazz);
        File parentFile = file.getParentFile();
        if (!parentFile.mkdirs() && !parentFile.exists())
            throw new RuntimeException("Create dir failed: " + parentFile.getAbsolutePath());
        logger.debug("Save class {} [loader={}] data to: {}", clazz.getName(), clazz.getClassLoader(), file.getAbsolutePath());
        IOUtils.writeBytes(file, data, false);
    }

    void remove(Class<?> clazz) {
        File classFile = getClassDataFile(clazz);
        classFile.delete();
        File parentFile = classFile.getParentFile();
        while (true) {
            if (parentFile != null &&
                    !dir.equals(parentFile.getAbsolutePath()) &&
                    parentFile.delete())
                parentFile = parentFile.getParentFile();
            else
                break;
        }
    }

    byte[] load(Class<?> clazz) {
        File file = getClassDataFile(clazz);
        if (file.exists()) {
            logger.debug("Load class {} [loader={}] data from: {}", clazz.getName(), clazz.getClassLoader(), file.getAbsolutePath());
            try {
                return IOUtils.readBytes(file);
            } catch (Exception e) {
                logger.error("Read class data file failed.", e);
                return null;
            }
        }
        return null;
    }
}
