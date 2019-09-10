package agent.server.transform.impl.utils;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.CtClass;

import java.net.URL;
import java.security.CodeSource;
import java.util.*;

@SuppressWarnings("unchecked")
public class ClassPathRecorder {
    private static final Logger logger = Logger.getLogger(ClassPoolUtils.class);
    private static final Collection<String> skipPackages = Collections.singleton("javassist.");

    private AgentClassPool cp;
    private final Map<URL, Class<?>> urlToClass = new HashMap<>();
    private final Map<ClassLoader, Set<String>> loaderToRefClassNames = new HashMap<>();

    ClassPathRecorder(AgentClassPool cp) {
        this.cp = cp;
    }

    public void add(Class<?> clazz) {
        add(Collections.singleton(clazz));
    }

    public void add(Collection<Class<?>> classSet) {
        if (!classSet.isEmpty()) {
            for (Class<?> clazz : classSet) {
                ClassLoader classLoader = clazz.getClassLoader();
                if (classLoader != null) {
                    addRefClassToPool(clazz);
                    loaderToRefClassNames.computeIfAbsent(classLoader, key -> new HashSet<>())
                            .addAll(getRefClassNames(clazz));
                }
            }
            loaderToRefClassNames.forEach((loader, refClassNames) ->
                    findRefClassSet(loader, refClassNames)
                            .forEach(this::addRefClassToPool)
            );
        }
    }

    void clear() {
        cp = null;
        urlToClass.clear();
        loaderToRefClassNames.clear();
    }

    private boolean isNativePackage(String namePath) {
        return ReflectionUtils.isJavaNativePackage(namePath)
                || skipPackages.stream().anyMatch(namePath::startsWith);
    }

    private Collection<String> getRefClassNames(Class<?> clazz) {
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

    private Collection<Class<?>> findRefClassSet(ClassLoader loader, Collection<String> refClassNameSet) {
        return Utils.wrapToRtError(() -> {
            Set<Class<?>> refClassSet = new HashSet<>();
            for (String refClassName : refClassNameSet) {
                logger.debug("Load class {} by loader: {}", refClassName, loader);
                Class<?> refClass = loader.loadClass(refClassName);
                refClassSet.add(refClass);
            }
            return refClassSet;
        });
    }

    private void addRefClassToPool(Class<?> refClass) {
        if (!isNativePackage(refClass.getName())) {
            CodeSource codeSource = refClass.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                URL location = codeSource.getLocation();
                if (!urlToClass.containsKey(location)) {
                    ClassPath classPath = new ClassClassPath(refClass);
                    logger.debug("Add class path to class pool: {}, code source url: {}", classPath, location);
                    cp.insertClassPath(classPath);
                    urlToClass.put(location, refClass);
                }
            } else
                logger.debug("No code source found for ref class: {}", refClass.getName());
        }
    }
}
