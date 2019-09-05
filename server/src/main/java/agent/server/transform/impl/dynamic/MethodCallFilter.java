package agent.server.transform.impl.dynamic;

public interface MethodCallFilter {
    boolean accept(MethodCallInfo methodCallInfo);
}
