package agent.server.transform.search.filter;

import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.search.InvokeChainSearcher.InvokeInfo;

public class InvokeChainFilter implements AgentFilter<InvokeInfo> {
    private final AgentFilter<Class<?>> classFilter;
    private final AgentFilter<DestInvoke> methodFilter;
    private final AgentFilter<DestInvoke> constructorFilter;
    private final int maxLevel;

    InvokeChainFilter(ClassFilter classFilter, InvokeFilter methodFilter, InvokeFilter constructorFilter, int maxLevel) {
        this.classFilter = classFilter;
        this.methodFilter = methodFilter;
        this.constructorFilter = constructorFilter;
        this.maxLevel = maxLevel;
    }

    @Override
    public boolean accept(InvokeInfo info) {
        if (info.getLevel() <= maxLevel) {
            Class<?> clazz = info.getInvokeClass();
            if (classFilter == null || classFilter.accept(clazz)) {
                DestInvoke invoke = info.getInvoke();
                return info.isConstructor() ?
                        constructorFilter == null || constructorFilter.accept(invoke) :
                        methodFilter == null || methodFilter.accept(invoke);
            }
        }
        return false;
    }
}
