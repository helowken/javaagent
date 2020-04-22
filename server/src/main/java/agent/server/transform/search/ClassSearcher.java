package agent.server.transform.search;

import agent.common.config.ClassFilterConfig;
import agent.server.transform.search.filter.FilterUtils;

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

}
