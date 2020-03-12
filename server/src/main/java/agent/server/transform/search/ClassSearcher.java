package agent.server.transform.search;

import agent.base.utils.Logger;
import agent.common.config.ClassFilterConfig;
import agent.server.transform.search.filter.ClassFilter;
import agent.server.transform.search.filter.FilterUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ClassSearcher {
    private static final Logger logger = Logger.getLogger(ClassSearcher.class);
    private static final ClassSearcher instance = new ClassSearcher();

    public static ClassSearcher getInstance() {
        return instance;
    }

    private ClassSearcher() {
    }

    public Collection<Class<?>> search(ClassLoader loader, ClassCache classCache, ClassFilterConfig classFilterConfig) {
        classFilterConfig.validate();
        Collection<String> includes = classFilterConfig.getIncludes();
        Collection<String> excludes = classFilterConfig.getExcludes();
        if (includes != null) {
            Set<Class<?>> classSet = new HashSet<>();
            ClassNamesAndRegexps includesItem = convert(includes);
            if (!includesItem.regexps.isEmpty()) {
                classSet.addAll(
                        classCache.findClasses(
                                FilterUtils.newClassFilter(
                                        includesItem.regexps,
                                        excludes,
                                        true
                                )
                        )
                );
            }
            if (!includesItem.classNames.isEmpty()) {
                Collection<Class<?>> classes = loadClasses(loader, includesItem.classNames);
                if (excludes != null) {
                    ClassFilter classFilter = FilterUtils.newClassFilter(
                            null,
                            excludes,
                            true
                    );
                    classes.stream()
                            .filter(classFilter::accept)
                            .forEach(classSet::add);
                } else
                    classSet.addAll(classes);
            }
            return classSet;
        }
        return classCache.findClasses(
                FilterUtils.newClassFilter(null, excludes, true)
        );
    }

    private Collection<Class<?>> loadClasses(ClassLoader loader, Collection<String> classNames) {
        Set<Class<?>> classSet = new HashSet<>();
        classNames.forEach(
                className -> {
                    try {
                        classSet.add(
                                loader.loadClass(className)
                        );
                    } catch (Exception e) {
                        logger.error("Load class {} failed", e, className);
                    }
                }
        );
        return classSet;
    }

    private ClassNamesAndRegexps convert(Collection<String> input) {
        Collection<String> classNames = new ArrayList<>();
        Collection<String> regexps = new ArrayList<>();
        input.forEach(
                s -> {
                    if (FilterUtils.isRegexp(s))
                        regexps.add(s);
                    else
                        classNames.add(s);
                }
        );
        return new ClassNamesAndRegexps(classNames, regexps);
    }

    private static class ClassNamesAndRegexps {
        final Collection<String> classNames;
        final Collection<String> regexps;

        private ClassNamesAndRegexps(Collection<String> classNames, Collection<String> regexps) {
            this.classNames = classNames;
            this.regexps = regexps;
        }
    }
}
