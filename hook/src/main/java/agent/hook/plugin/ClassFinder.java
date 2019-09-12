package agent.hook.plugin;

import java.util.Map;

public interface ClassFinder {
    ClassLoader findClassLoader(String contextPath);

    void setParentClassLoader(String contextPath, ClassLoader parentLoader);

    Class<?> findClass(String contextPath, String className);

    Map<String, ClassLoader> getContextToLoader();
}
