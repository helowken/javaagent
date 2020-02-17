package agent.server.transform.cache;

public interface ClassFilter {
    boolean accept(Class<?> clazz);
}
