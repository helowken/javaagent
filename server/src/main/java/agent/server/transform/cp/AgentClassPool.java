package agent.server.transform.cp;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.MethodSignatureUtils;
import agent.base.utils.ReflectionUtils;
import javassist.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public class AgentClassPool {
    private static final Logger logger = Logger.getLogger(AgentClassPool.class);
    private static Collection<String> skipPackages = Collections.unmodifiableList(
            Arrays.asList(
                    "javassist.",
                    "agent."
            )
    );
    private final LockObject cpLock = new LockObject();
    private ClassPool cp = ClassPool.getDefault();
    private Set<ClassPath> classPathSet = new HashSet<>();
    private Set<CtClass> classSet = new HashSet<>();
    private Map<Class<?>, byte[]> classToData = new HashMap<>();

    public static boolean isNativePackage(String namePath) {
        return ReflectionUtils.isJavaNativePackage(namePath)
                || skipPackages.stream().anyMatch(namePath::startsWith);
    }

    public AgentClassPool(String context) {
        addClassPath(
                new InMemoryClassPath(context)
        );
        addClassPath(
                new ClassClassPath(
                        getClass()
                )
        );
    }

    private void addClassPath(ClassPath classPath) {
        cpLock.sync(lock -> {
            classPathSet.add(classPath);
            cp.appendClassPath(classPath);
        });
    }

    public CtClass get(String className) {
        return get(new String[]{className})[0];
    }

    public CtClass[] get(String[] classNames) {
        return cpLock.syncValue(lock -> {
            CtClass[] ctClasses = cp.get(classNames);
            Stream.of(ctClasses).forEach(CtClass::defrost);
            Collections.addAll(classSet, ctClasses);
            return ctClasses;
        });
    }

    public void saveClassData(Class<?> clazz, byte[] data) {
        cpLock.sync(
                lock -> classToData.put(clazz, data)
        );
    }

    public byte[] getClassData(Class<?> clazz) throws Exception {
        return classToData.getOrDefault(
                clazz,
                get(clazz.getName()).toBytecode()
        );
    }

    public void clear() {
        cpLock.sync(lock -> {
            removeAllClassPaths();
            detachAllClasses();
            classToData.clear();
        });
    }

    private void removeAllClassPaths() {
        classPathSet.forEach(classPath -> {
//            logger.debug("remove class path: {}", classPath);
            cp.removeClassPath(classPath);
        });
        classPathSet.clear();
    }

    private void detachAllClasses() {
        classSet.forEach(ctClass -> {
//            logger.debug("detach class: {}", ctClass.getName());
            ctClass.detach();
        });
        classSet.clear();
    }

    public CtMethod getMethod(Method method) {
        return getMethod(
                method.getDeclaringClass().getName(),
                method.getName(),
                MethodSignatureUtils.getSignature(method)
        );
    }

    public CtMethod getMethod(String className, String methodName, String methodSignature) {
        CtMethod[] ctMethods = get(className).getDeclaredMethods();
        if (ctMethods != null) {
            for (CtMethod ctMethod : ctMethods) {
                if (ctMethod.getName().equals(methodName) &&
                        methodSignature.equals(ctMethod.getSignature()))
                    return ctMethod;
            }
        }
        throw new RuntimeException("No method found by: " + className + "." + methodName + methodSignature);
    }
}
