package agent.server.transform.impl.dynamic;

import java.util.Collection;

public interface MethodRuleFilter {
    boolean accept(MethodInfo methodInfo);

    boolean stepInto(MethodInfo methodInfo);

    default FindImplClassPolicy getFindImplClassPolicy() {
        return FindImplClassPolicy.FROM_LOADED_CLASSES;
    }

    default Collection<String> getImplClasses(MethodInfo methodInfo) {
        return null;
    }

    enum FindImplClassPolicy {
        FROM_LOADED_CLASSES,
        USER_DEFINED
    }
}
