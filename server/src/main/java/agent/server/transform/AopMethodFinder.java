package agent.server.transform;

import agent.server.transform.impl.invoke.DestInvoke;

import java.lang.reflect.Method;
import java.util.Set;

public interface AopMethodFinder {
    Set<Method> findMethods(Method targetMethod, ClassLoader classLoader);
}
