package agent.server.transform.impl.dynamic;

public interface MethodFilter {
    boolean accept(MethodInfo methodInfo);
}
