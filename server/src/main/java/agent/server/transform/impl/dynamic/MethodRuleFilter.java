package agent.server.transform.impl.dynamic;

import java.util.Map;

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

    default Map<String, Class<?>> getImplClasses(MethodInfo methodInfo, SubTypeSearcher subClassSearcher) {
        return subClassSearcher.get();
    }

}
