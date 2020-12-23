package agent.server.transform.search.filter;

import java.util.Collection;

abstract class AbstractClassFilter implements ClassFilter {

    static ClassFilter include(Collection<String> strings) {
        return new ClassNameFilter(
                PatternFilter.include(strings)
        );
    }

    static ClassFilter exclude(Collection<String> strings) {
        return new ClassNameFilter(
                PatternFilter.exclude(strings)
        );
    }

    private static class ClassNameFilter extends AbstractClassFilter {
        private final AgentFilter<String> filter;

        private ClassNameFilter(AgentFilter<String> filter) {
            this.filter = filter;
        }

        @Override
        public boolean accept(Class<?> v) {
            return filter.accept(
                    v.getName()
            );
        }

        @Override
        public String toString() {
            return "ClassNameFilter{" +
                    "filter=" + filter +
                    '}';
        }
    }
}
