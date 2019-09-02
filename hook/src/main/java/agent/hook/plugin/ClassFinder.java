package agent.hook.plugin;

public interface ClassFinder {
    ClassLoader findClassLoader(String contextPath);

    void setParentClassLoader(String contextPath, ClassLoader parentLoader);

    Class<?> findClass(String contextPath, String className);
}
