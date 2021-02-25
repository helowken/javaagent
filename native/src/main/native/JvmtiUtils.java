package agent.jvmti;

import java.io.File;
import java.util.List;

public class JvmtiUtils {
    private native List<Object> findObjectsByClassHelper(Class<?> clazz, int maxCount);

    private native List<Class<?>> getLoadedClasses();

    private native boolean tryToSetEuidAndEgid(int pid);

    private native int getProcId();
}
