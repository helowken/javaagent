package agent.builtin.tools.result.filter;

import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

public class ResultFilterData<T> {
    public final InvokeMetadata metadata;
    public final T data;
    public final int level;

    public ResultFilterData(InvokeMetadata metadata, T data) {
        this(metadata, data, 0);
    }

    public ResultFilterData(InvokeMetadata metadata, T data, int level) {
        this.metadata = metadata;
        this.data = data;
        this.level = level;
    }
}
