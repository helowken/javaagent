package agent.server.transform.impl;

import agent.base.utils.Pair;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.config.FilterConfig;

import java.util.*;

public class ClassesToConfig {
    private final List<Pair<Collection<Class<?>>, ClassConfig>> ps = new ArrayList<>();

    public void add(Collection<Class<?>> classes, ClassConfig classConfig) {
        ps.add(
                new Pair<>(classes, classConfig)
        );
    }

    public Map<Class<?>, Collection<FilterConfig>> getClassToFilters() {
        Map<Class<?>, Collection<FilterConfig>> rsMap = new HashMap<>();
        ps.forEach(
                p -> p.left.forEach(
                        clazz -> {
                            Collection<FilterConfig> filters = rsMap.computeIfAbsent(
                                    clazz,
                                    key -> new ArrayList<>()
                            );

                            Optional.ofNullable(
                                    p.right.getMethodFilter()
                            ).ifPresent(filters::add);

                            Optional.ofNullable(
                                    p.right.getConstructorFilter()
                            ).ifPresent(filters::add);
                        }
                )
        );
        return rsMap;
    }
}
