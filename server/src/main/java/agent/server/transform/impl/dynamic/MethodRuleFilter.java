package agent.server.transform.impl.dynamic;

import java.util.Collection;

public interface MethodRuleFilter {
    default boolean accept(MethodInfo methodInfo) {
        return !methodInfo.isNative;
    }

    default boolean stepInto(MethodInfo methodInfo) {
        return !methodInfo.isNative;
    }

    default boolean needGetImplClasses(MethodInfo methodInfo) {
        return !methodInfo.isNative;
    }

    default Collection<String> getImplClasses(MethodInfo methodInfo, Collection<String> loadedSubClassNames) {
        return loadedSubClassNames;
    }
}
