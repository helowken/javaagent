package agent.builtin.tools.result.filter;

import agent.base.utils.Pair;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;
import agent.server.transform.search.filter.AgentFilter;

public interface ResultFilter<T> extends AgentFilter<Pair<InvokeMetadata, T>> {
}
