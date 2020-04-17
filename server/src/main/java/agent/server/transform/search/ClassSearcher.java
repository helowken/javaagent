package agent.server.transform.search;

import agent.common.config.ClassFilterConfig;
import agent.server.transform.search.filter.FilterUtils;

import java.util.ArrayList;
import java.util.Collection;

public class ClassSearcher {
    private static final ClassSearcher instance = new ClassSearcher();

    public static ClassSearcher getInstance() {
        return instance;
    }

    private ClassSearcher() {
    }

    public Collection<Class<?>> search(ClassCache classCache, ClassFilterConfig classFilterConfig) {
        classFilterConfig.validate();
        return classCache.findClasses(
                FilterUtils.newClassFilter(
                        classFilterConfig.getIncludes(),
                        classFilterConfig.getExcludes(),
                        true)
        );
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
