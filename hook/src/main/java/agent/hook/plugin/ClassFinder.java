package agent.hook.plugin;

public interface ClassFinder {
    ClassLoader findClassLoader(String contextPath);

    Class<?> findClass(String contextPath, String className);
}
