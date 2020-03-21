package agent.server.transform;

import java.lang.reflect.Method;
import java.util.Collection;

public interface AopMethodFinder {
    Collection<Method> findMethods(Method targetMethod, ClassLoader classLoader);
}
