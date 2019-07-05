package agent.server.transform.impl;

import agent.base.utils.ClassUtils;
import agent.base.utils.Logger;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;

import java.net.URL;
import java.security.CodeSource;
import java.util.*;

@SuppressWarnings("unchecked")
public class ClassPoolUtils {
    private static final Logger logger = Logger.getLogger(ClassPoolUtils.class);
    private static final Collection<String> skipPackages = Collections.singleton("javassist.");

    public static <T> T exec(Class<?> clazz, ValueFunc<T> func) throws Exception {
        return exec(Collections.singleton(clazz), func);
    }

    public static <T> T exec(Set<Class<?>> classSet, ValueFunc<T> func) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        List<ClassPath> classPathList = new ArrayList<>();
        Map<URL, Class<?>> urlToClass = new HashMap<>();
        Map<ClassLoader, Set<String>> loaderToRefClassNames = new HashMap<>();
        for (Class<?> clazz : classSet) {
            addRefClassToPool(cp, classPathList, urlToClass, clazz);
            loaderToRefClassNames.computeIfAbsent(clazz.getClassLoader(), key -> new HashSet<>())
                    .addAll(getRefClassNames(cp, clazz));
        }
        loaderToRefClassNames.forEach((loader, refClassNames) ->
                findRefClassSet(loader, refClassNames)
                        .forEach(refClass ->
                                addRefClassToPool(cp, classPathList, urlToClass, refClass)
                        )
        );
        try {
            return func.exec(cp);
        } finally {
            classPathList.forEach(classPath -> {
                logger.debug("Remove class path from class pool: {}", classPath);
                cp.removeClassPath(classPath);
            });
        }
    }

    private static boolean isNativePackage(String namePath) {
        return ClassUtils.isJavaNativePackage(namePath)
                || skipPackages.stream().anyMatch(namePath::startsWith);
    }

    private static Collection<String> getRefClassNames(ClassPool cp, Class<?> clazz) throws Exception {
        Set<String> refClassNameSet = new HashSet<>();
        String currClassName = clazz.getName();
        CtClass cc = cp.get(currClassName);
        cc.getRefClasses().forEach(refClass -> {
            if (refClass instanceof String) {
                String refClassName = (String) refClass;
                if (!isNativePackage(refClassName))
                    refClassNameSet.add(refClassName);
            } else
                logger.debug("Unknown ref class: {}, type: {}", refClass, refClass == null ? null : refClass.getClass());
        });
        refClassNameSet.remove(currClassName);
        return refClassNameSet;
    }

    private static Collection<Class<?>> findRefClassSet(ClassLoader loader, Collection<String> refClassNameSet) {
        try {
            Set<Class<?>> refClassSet = new HashSet<>();
            for (String refClassName : refClassNameSet) {
                logger.debug("Load class {} by loader: {}", refClassName, loader);
                Class<?> refClass = loader.loadClass(refClassName);
                refClassSet.add(refClass);
            }
            return refClassSet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addRefClassToPool(ClassPool cp, List<ClassPath> classPathList, Map<URL, Class<?>> urlToClass, Class<?> refClass) {
        if (!isNativePackage(refClass.getName())) {
            CodeSource codeSource = refClass.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                URL location = codeSource.getLocation();
                if (!urlToClass.containsKey(location)) {
                    ClassPath classPath = new ClassClassPath(refClass);
                    logger.debug("Add class path to class pool: {}, code source url: {}", classPath, location);
                    cp.insertClassPath(classPath);
                    classPathList.add(classPath);
                    urlToClass.put(location, refClass);
                }
            } else
                logger.debug("No code source found for ref class: {}", refClass.getName());
        }
    }

    public interface ValueFunc<T> {
        T exec(ClassPool cp) throws Exception;
    }
}
