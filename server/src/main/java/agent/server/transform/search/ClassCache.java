package agent.server.transform.search;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.server.transform.InstrumentationMgr;
import agent.server.transform.TransformMgr;
import agent.server.transform.search.filter.ClassFilter;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClassCache {
    private static final Logger logger = Logger.getLogger(ClassCache.class);
    private static Collection<String> skipPackages = Collections.unmodifiableList(
            Arrays.asList(
                    "org.objectweb.asm.",
                    "agent."
            )
    );

    private final Map<Class<?>, List<Class<?>>> classToSubTypes = new ConcurrentHashMap<>();
    private volatile List<Class<?>> loadedClasses;

    static boolean isNativePackage(String namePath) {
        return ReflectionUtils.isJavaNativePackage(namePath)
                || skipPackages.stream().anyMatch(namePath::startsWith);
    }

    private Collection<Class<?>> getLoadedClasses() {
        if (loadedClasses == null) {
            synchronized (this) {
                if (loadedClasses == null)
                    loadedClasses = Arrays.stream(
                            InstrumentationMgr.getInstance().getAllLoadedClasses()
                    )
                            .filter(
                                    clazz -> !isNativePackage(
                                            clazz.getName()
                                    )
                            )
                            .collect(
                                    Collectors.toList()
                            );
            }
        }
        return loadedClasses;
    }

    public Collection<Class<?>> getSubTypes(Class<?> baseClass, ClassFilter filter) {
        if (Modifier.isFinal(baseClass.getModifiers()))
            return Collections.emptyList();

        Collection<Class<?>> subTypes = classToSubTypes.computeIfAbsent(
                baseClass,
                clazz -> ReflectionUtils.findSubTypes(
                        clazz,
                        getLoadedClasses()
                )
        );

        return filter == null ?
                subTypes :
                subTypes.stream()
                        .filter(filter::accept)
                        .collect(
                                Collectors.toList()
                        );
    }

    public Collection<Class<?>> getSubClasses(Class<?> baseClass, ClassFilter filter) {
        return ReflectionUtils.findSubClasses(
                baseClass,
                getSubTypes(baseClass, filter)
        );
    }

    public Collection<Class<?>> findClasses(ClassFilter filter) {
        return getLoadedClasses()
                .stream()
                .filter(
                        clazz -> filter == null || filter.accept(clazz)
                )
                .collect(
                        Collectors.toList()
                );
    }

}
