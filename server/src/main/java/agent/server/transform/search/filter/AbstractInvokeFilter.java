package agent.server.transform.search.filter;


import java.util.Collection;

abstract class AbstractInvokeFilter implements InvokeFilter {

    static InvokeFilter include(Collection<String> strings) {
        return new InvokeKeyFilter(
                PatternFilter.include(strings)
        );
    }

    static InvokeFilter exclude(Collection<String> strings) {
        return new InvokeKeyFilter(
                PatternFilter.exclude(strings)
        );
    }

    private static class InvokeKeyFilter extends AbstractInvokeFilter {
        private final AgentFilter<String> filter;

        private InvokeKeyFilter(AgentFilter<String> filter) {
            this.filter = filter;
        }

        @Override
        public boolean accept(String v) {
            return filter.accept(v);
        }
    }
}
