package agent.server.transform.impl;

import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassPoolUtils {
    public static <T> T exec(Class<?> clazz, ValueFunc<T> func) throws Exception {
        return exec(Collections.singleton(clazz), func);
    }

    public static <T> T exec(Set<Class<?>> classSet, ValueFunc<T> func) throws Exception {
        List<ClassPath> classPathList = classSet.stream()
                .map(ClassClassPath::new)
                .collect(Collectors.toList());
        ClassPool cp = ClassPool.getDefault();
        classPathList.forEach(cp::insertClassPath);
        try {
            return func.exec(cp);
        } finally {
            classPathList.forEach(cp::removeClassPath);
        }
    }

    public interface ValueFunc<T> {
        T exec(ClassPool cp) throws Exception;
    }
}
