package agent.server.transform.impl.dynamic;

public interface MethodRuleFilter {
    boolean accept(MethodInfo methodInfo);

    boolean stepInto(MethodInfo methodInfo);
}
