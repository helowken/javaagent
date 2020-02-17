package agent.server.transform.impl;

import agent.server.transform.cache.ClassCache;
import agent.server.transform.config.FilterConfig;

import java.util.*;

public class TransformShareInfo {
    private final String context;
    private final Map<Class<?>, Collection<FilterConfig>> classToFilters;
    private final ClassCache classCache;

    public TransformShareInfo(String context, ClassesToConfig classesToConfig, ClassCache classCache) {
        this.context = context;
        this.classToFilters = classesToConfig.getClassToFilters();
        this.classCache = classCache;
    }

    public String getContext() {
        return context;
    }

    public ClassCache getClassCache() {
        return classCache;
    }

    Collection<FilterConfig> getInvokeFilters(Class<?> clazz) {
        return Optional.ofNullable(
                classToFilters.get(clazz)
        ).orElseThrow(
                () -> new RuntimeException("No invoke filters found by class: " + clazz.getName())
        );
    }

    public Map<Class<?>, Collection<FilterConfig>> getClassToInvokeFilters() {
        return Collections.unmodifiableMap(classToFilters);
    }

    public Collection<Class<?>> getTargetClasses() {
        return new ArrayList<>(
                classToFilters.keySet()
        );
    }

}
