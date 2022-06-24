package agent.jvmti;

import java.io.File;
import java.util.List;

public class JvmtiUtils {
    private native List<Object> findObjectsByClassHelper(Class<?> clazz, int maxCount);

    private native List<Class<?>> getLoadedClasses();

    private native boolean changeCredential(int pid);

    private native boolean resetCredential();

    private native int getProcId();

    private native boolean attachJvm(int pid, String jarPath, String args);
}
