package agent.server.transform.impl.utils;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;

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
}
