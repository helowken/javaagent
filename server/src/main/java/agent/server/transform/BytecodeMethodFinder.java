package agent.server.transform;

import agent.server.transform.impl.dynamic.MethodInfo;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Function;

public interface BytecodeMethodFinder {
    Set<Method> findBytecodeMethods(MethodInfo targetMethodInfo, ClassLoader classLoader, Function<MethodInfo, Method> methodGetter);
}
