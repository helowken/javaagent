package agent.server;

import agent.base.utils.IndentUtils;
import agent.base.utils.ReflectionUtils;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.TransformMgr;

import java.util.*;

public class TestClassLoaderCascade {
    @SuppressWarnings("unchecked")
    public static void test() {
        List<Class<? extends ClassLoader>> loaderClassList = (List) ReflectionUtils.findSubTypes(
                ClassLoader.class,
                Arrays.asList(
                        TransformMgr.getInstance().getAllLoadedClasses()
                )
        );

        List<ClassLoader> loaderList = new ArrayList<>();
        loaderClassList.forEach(loaderClass ->
                loaderList.addAll(
                        JvmtiUtils.getInstance().findObjectsByClass(loaderClass, Integer.MAX_VALUE)
                )
        );

        Map<ClassLoader, List<ClassLoader>> parentToChildren = new HashMap<>();
        List<ClassLoader> rootLoaders = new ArrayList<>();
        loaderList.forEach(
                loader -> {
                    ClassLoader parentLoader = loader.getParent();
                    if (parentLoader != null) {
                        parentToChildren.computeIfAbsent(
                                parentLoader,
                                key -> new ArrayList<>()
                        ).add(loader);
                    } else
                        rootLoaders.add(loader);
                }
        );

        rootLoaders.forEach(
                rootLoader -> printLoaderTree(0, rootLoader, parentToChildren)
        );
    }

    private static void printLoaderTree(int level, ClassLoader loader, Map<ClassLoader, List<ClassLoader>> parentToChildren) {
        Class<?>[] classes = TransformMgr.getInstance().getInitiatedClasses(loader);
        int count = classes.length;
        System.out.println(IndentUtils.getIndent(level) + loader.getClass().getName() + System.identityHashCode(loader) + ", Loaded Class Count: " + count);
        Optional.ofNullable(
                parentToChildren.get(loader)
        ).ifPresent(
                childLoaders -> childLoaders.forEach(
                        childLoader -> printLoaderTree(level + 1, childLoader, parentToChildren)
                )
        );


    }
}
