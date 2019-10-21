package agent.server.transform.impl.utils;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.MethodSignatureUtils;
import agent.base.utils.Utils;
import agent.server.transform.ClassDataFinder;
import javassist.*;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class AgentClassPool {
    private static final Logger logger = Logger.getLogger(AgentClassPool.class);
    private static final AgentClassPool instance = new AgentClassPool();

    private final LockObject cpLock = new LockObject();
    private ClassPool cp = ClassPool.getDefault();
    private Set<ClassPath> classPathSet = new HashSet<>();
    private Set<CtClass> classSet = new HashSet<>();

    public static AgentClassPool getInstance() {
        return instance;
    }

    private AgentClassPool() {
    }

    public void insertClassPath(ClassPath classPath) {
        cpLock.sync(lock -> {
            classPathSet.add(classPath);
            cp.insertClassPath(classPath);
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

    public CtClass get(Class<?> clazz) {
        String className = clazz.getName();
        try {
            return cp.get(className);
        } catch (NotFoundException e) {
            return Utils.wrapToRtError(() -> {
                byte[] classData = ClassDataFinder.getInstance().getClassData(clazz);
                if (classData != null) {
                    insertClassPath(
                            new InMemoryClassPath(clazz, classData)
                    );
                    return get(className);
                }
                throw e;
            });
        }
    }

    void clear() {
        cpLock.sync(lock -> {
            removeAllClassPaths();
            detachAllClasses();
        });
    }

    private void removeAllClassPaths() {
        classPathSet.forEach(classPath -> {
            logger.debug("remove class path: {}", classPath);
            cp.removeClassPath(classPath);
        });
        classPathSet.clear();
    }

    private void detachAllClasses() {
        classSet.forEach(ctClass -> {
            logger.debug("detach class: {}", ctClass.getName());
            ctClass.detach();
        });
        classSet.clear();
    }

    public CtMethod getMethod(Method method) {
        return getMethod(
                method.getDeclaringClass(),
                method.getName(),
                MethodSignatureUtils.getSignature(method)
        );
    }

    public CtMethod getMethod(Class<?> clazz, String methodName, String methodSignature) {
        CtMethod[] ctMethods = get(clazz).getDeclaredMethods();
        if (ctMethods != null) {
            for (CtMethod ctMethod : ctMethods) {
                if (ctMethod.getName().equals(methodName) &&
                        methodSignature.equals(ctMethod.getSignature()))
                    return ctMethod;
            }
        }
        throw new RuntimeException("No method found by: " + clazz.getName() + "." + methodName + methodSignature);
    }
}
