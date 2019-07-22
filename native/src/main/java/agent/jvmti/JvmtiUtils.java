package agent.jvmti;


import agent.base.utils.Logger;

public class JvmtiUtils {
    private static final Logger logger = Logger.getLogger(JvmtiUtils.class);
    private static final JvmtiUtils instance = new JvmtiUtils();

    static {
        System.load("/home/helowken/test_jni/jni_jvmti/libagent_jvmti_JvmtiUtils.so");
    }

    public static JvmtiUtils getInstance() {
        return instance;
    }

    private JvmtiUtils() {
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
