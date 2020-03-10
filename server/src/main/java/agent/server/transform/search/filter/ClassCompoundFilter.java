package agent.server.transform.search.filter;

import java.util.Collection;

class ClassCompoundFilter extends CompoundFilter<Class<?>, ClassFilter> implements ClassFilter {
    ClassCompoundFilter(Collection<ClassFilter> filters) {
        super(filters);
    }
}
