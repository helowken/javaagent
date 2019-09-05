package agent.server.transform.impl.dynamic;

import agent.common.utils.Registry;

import java.util.function.Function;

public class MethodInfo extends AbstractMethodInfo {
    private Registry<String, MethodCallInfo> registry = new Registry<>();

    MethodInfo(String className, String methodName, String signature) {
        super(className, methodName, signature);
    }

    MethodCallInfo regIfAbsent(String key, Function<String, MethodCallInfo> supplier) {
        return registry.regIfAbsent(key, supplier);
    }

    MethodCallInfo get(String key) {
        return registry.get(key);
    }
}
