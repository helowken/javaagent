package agent.server.transform.impl.dynamic;

import java.util.Collection;

public interface MethodRuleFilter {
    boolean accept(MethodInfo methodInfo);

    boolean stepInto(MethodInfo methodInfo);

    default Collection<String> getImplClasses(MethodInfo methodInfo) {
        return null;
    }
}
