package agent.server.transform;

import agent.base.utils.Utils;
import agent.server.transform.cache.ClassCache;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.ClassFilterConfig;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ClassSearcher {
    private final ClassLoader loader;
    private final ClassCache classCache;

    public static Collection<Class<?>> search(ClassLoader loader, ClassCache classCache, ClassConfig classConfig) {
        return new ClassSearcher(loader, classCache).search(classConfig);
    }

    private ClassSearcher(ClassLoader loader, ClassCache classCache) {
        this.loader = loader;
        this.classCache = classCache;
    }

    private Collection<Class<?>> search(ClassConfig classConfig) {
        Set<Class<?>> classSet = new HashSet<>();
        ClassFilterConfig classFilterConfig = Optional.ofNullable(
                classConfig.getClassFilter()
        ).orElseThrow(
                () -> new RuntimeException("No class filter found: " + classConfig)
        );

        Optional.ofNullable(
                classFilterConfig.getClasses()
        ).ifPresent(
                classes -> collectTargetClasses(classes, classSet)
        );

        if (classFilterConfig.getIncludes() != null)
            collectMatchedClasses(
                    classFilterConfig.getIncludes(),
                    classFilterConfig.getExcludes(),
                    classSet
            );
        return classSet;
    }

    private void collectTargetClasses(Collection<String> classNames, Set<Class<?>> classSet) {
        classNames.forEach(
                className -> collectClassOrInterface(
                        loadClass(className),
                        classSet
                )
        );
    }

    private void collectMatchedClasses(Collection<String> includes, Collection<String> excludes, Set<Class<?>> classSet) {
        classCache.findClasses(loader, includes, excludes, true).forEach(
                clazz -> collectClassOrInterface(clazz, classSet)
        );
    }

    private void collectClassOrInterface(Class<?> clazz, Set<Class<?>> classSet) {
        if (clazz.isInterface())
            classSet.addAll(
                    classCache.getSubTypes(loader, clazz, false)
            );
        else
            classSet.add(clazz);
    }

    private Class<?> loadClass(String className) {
        return Utils.wrapToRtError(
                () -> loader.loadClass(className)
        );
    }
}
