package agent.server.transform.search.filter;

import agent.server.transform.impl.invoke.DestInvoke;

import java.util.Collection;

class InvokeCompoundFilter extends CompoundFilter<DestInvoke, InvokeFilter> implements InvokeFilter {
    InvokeCompoundFilter(Collection<InvokeFilter> filters) {
        super(filters);
    }
}
