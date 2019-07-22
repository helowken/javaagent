package agent.jvmti;


import agent.base.utils.Logger;

import java.io.File;
import java.util.List;

public class JvmtiUtils {
    private static final Logger logger = Logger.getLogger(JvmtiUtils.class);
    private static final JvmtiUtils instance = new JvmtiUtils();

    public static JvmtiUtils getInstance() {
        return instance;
    }

    private JvmtiUtils() {
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

    public native Object findObjectByClass(Class<?> clazz);
}
