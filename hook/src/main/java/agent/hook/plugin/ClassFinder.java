package agent.hook.plugin;

public interface ClassFinder {
    Class<?> findClass(String contextPath, String className);
}
