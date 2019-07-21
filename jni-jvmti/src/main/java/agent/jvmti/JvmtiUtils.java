package agent.jvmti;

public class JvmtiUtils {
    private static final JvmtiUtils instance = new JvmtiUtils();

    static {
        System.load("/home/helowken/test_jni/jni_jvmti/libagent_jvmti_JvmtiUtils.so");
    }

    public static JvmtiUtils getInstance() {
        return instance;
    }

    private JvmtiUtils() {
    }

    public native Object findObjectByClass(Class<?> clazz);
}
