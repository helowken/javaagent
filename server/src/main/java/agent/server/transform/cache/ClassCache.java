package agent.server.transform.cache;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.ReflectionUtils;
import agent.server.transform.TransformMgr;
import agent.server.tree.Node;
import agent.server.tree.TreeUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClassCache {
    private static Collection<String> skipPackages = Collections.unmodifiableList(
            Arrays.asList(
                    "javassist.",
                    "org.objectweb.asm.",
                    "agent."
            )
    );

    private final Map<ClassLoader, ClassFilter> loaderToFilter;
    private volatile Map<ClassLoader, Node<ClassCacheItem>> loaderToRoot;
    private final Map<Class<?>, List<Class<?>>> classToSubTypes = new ConcurrentHashMap<>();

    public static boolean isNativePackage(String namePath) {
        return ReflectionUtils.isJavaNativePackage(namePath)
                || skipPackages.stream().anyMatch(namePath::startsWith);
    }

    public static ClassFilter newClassFilter(Collection<String> includes, Collection<String> excludes, boolean includeInterface) {
        List<ClassFilter> filters = new ArrayList<>();
        if (includes != null)
            filters.add(
                    new IncludeClassFilter(includes)
            );
        if (excludes != null)
            filters.add(
                    new ExcludeClassFilter(excludes)
            );
        if (!includeInterface)
            filters.add(
                    NotInterfaceClassFilter.getInstance()
            );

        int size = filters.size();
        if (size > 0) {
            if (size > 1)
                return new CompoundClassFilter(filters);
            return filters.get(0);
        }
        return null;
    }

    public ClassCache(Map<ClassLoader, ClassFilter> loaderToFilter) {
        this.loaderToFilter = loaderToFilter;
    }

    private Map<ClassLoader, Node<ClassCacheItem>> getLoaderToRoot() {
        if (loaderToRoot == null) {
            synchronized (this) {
                if (loaderToRoot == null)
                    loaderToRoot = createNodes();
            }
        }
        return loaderToRoot;
    }

    private Map<ClassLoader, Node<ClassCacheItem>> createNodes() {
        Class<?>[] classes = TransformMgr.getInstance().getAllLoadedClasses();
        Map<ClassLoader, Map<ClassLoader, Node<ClassCacheItem>>> loaderToLoaderToNode = new HashMap<>();
        if (classes != null) {
            for (Class<?> clazz : classes) {
                if (!isNativePackage(clazz.getName())) {
                    ClassLoader classLoader = clazz.getClassLoader();
                    for (Map.Entry<ClassLoader, ClassFilter> entry : loaderToFilter.entrySet()) {
                        ClassLoader loader = entry.getKey();
                        ClassFilter filter = entry.getValue();
                        if (ClassLoaderUtils.isSelfOrDescendant(loader, classLoader) &&
                                (filter == null || filter.accept(clazz))) {
                            Map<ClassLoader, Node<ClassCacheItem>> loaderToNode = loaderToLoaderToNode.computeIfAbsent(
                                    loader,
                                    key -> new HashMap<>()
                            );
                            getOrCreateNode(loaderToNode, loader, classLoader)
                                    .getData()
                                    .add(clazz);
                            break;
                        }
                    }
                }
            }
        }
        return loaderToFilter.keySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                loader -> loader,
                                loader -> loaderToLoaderToNode.computeIfAbsent(
                                        loader,
                                        key -> Collections.emptyMap()
                                ).computeIfAbsent(
                                        loader,
                                        key -> new Node<>(
                                                new ClassCacheItem()
                                        )
                                )
                        )
                );
    }

    private Node<ClassCacheItem> getOrCreateNode(Map<ClassLoader, Node<ClassCacheItem>> loaderToNode,
                                                 ClassLoader baseLoader, ClassLoader subLoader) {
        return loaderToNode.computeIfAbsent(
                subLoader,
                classLoader -> {
                    Node<ClassCacheItem> node = new Node<>(
                            new ClassCacheItem()
                    );
                    if (classLoader != null) {
                        if (classLoader != baseLoader)
                            getOrCreateNode(
                                    loaderToNode,
                                    baseLoader,
                                    classLoader.getParent()
                            ).appendChild(node);
                    }
                    return node;
                }
        );
    }

    public Collection<Class<?>> getSubTypes(ClassLoader loader, Class<?> baseClass, boolean includeInterface) {
        Collection<Class<?>> classes = classToSubTypes.computeIfAbsent(
                baseClass,
                key -> Optional.ofNullable(
                        getLoaderToRoot().get(loader)
                ).map(
                        node -> findSubTypes(baseClass, node)
                ).orElse(
                        Collections.emptyList()
                )
        );
        return includeInterface ?
                classes :
                classes.stream()
                        .filter(
                                NotInterfaceClassFilter.getInstance()::accept
                        )
                        .collect(
                                Collectors.toList()
                        );
    }

    public Collection<Class<?>> getSubClasses(ClassLoader loader, Class<?> baseClass, boolean includeInterface) {
        return ReflectionUtils.findSubClasses(
                baseClass,
                getSubTypes(loader, baseClass, includeInterface)
        );
    }

    private List<Class<?>> findSubTypes(Class<?> baseClass, Node<ClassCacheItem> node) {
        List<Class<?>> rsList = new ArrayList<>();
        TreeUtils.traverse(
                node,
                n -> rsList.addAll(
                        ReflectionUtils.findSubTypes(
                                baseClass,
                                n.getData().classList
                        )
                )
        );
        return rsList;
    }

    public Collection<Class<?>> findClasses(ClassLoader loader, Collection<String> includes, boolean includeInterface) {
        return Optional.ofNullable(
                getLoaderToRoot().get(loader)
        ).map(
                node -> {
                    ClassFilter filter = newClassFilter(includes, null, includeInterface);
                    List<Class<?>> rsList = new ArrayList<>();
                    if (filter != null)
                        TreeUtils.traverse(
                                node,
                                n -> n.getData().classList
                                        .stream()
                                        .filter(filter::accept)
                                        .forEach(rsList::add)
                        );
                    return rsList;
                }
        ).orElse(
                Collections.emptyList()
        );
    }

    private class ClassCacheItem {
        private final List<Class<?>> classList = new ArrayList<>(1000);

        void add(Class<?> clazz) {
            classList.add(clazz);
        }
    }

}
