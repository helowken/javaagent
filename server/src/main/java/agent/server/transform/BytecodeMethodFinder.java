package agent.server.transform;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Function;

public interface BytecodeMethodFinder {
    Set<Method> findBytecodeMethods(Object targetMethodInfo, ClassLoader classLoader, Function<Object, Method> methodGetter);
}
