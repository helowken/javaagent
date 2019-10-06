package agent.server.transform.impl.dynamic;

import java.util.Map;

public interface MethodRuleFilter {
    default boolean accept(MethodInfo methodInfo) {
        return !methodInfo.isNativePackage;
    }

    default boolean stepInto(MethodInfo methodInfo) {
        return !methodInfo.isNativePackage;
    }

    default boolean needGetOverrideMethods(MethodInfo methodInfo) {
        return !methodInfo.isNativePackage;
    }

    default boolean needGetBytecodeMethods(MethodInfo methodInfo) {
        return !methodInfo.isNativePackage;
    }

    default Map<String, Class<?>> getImplClasses(MethodInfo methodInfo, SubTypeSearcher subClassSearcher) {
        return subClassSearcher.get();
    }

}
