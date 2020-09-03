package agent.server.transform.search.filter;

import agent.invoke.DestInvoke;
import agent.server.transform.search.InvokeChainSearcher.InvokeInfo;

abstract class AbstractInvokeChainFilter implements AgentFilter<InvokeInfo> {
    private final AgentFilter<Class<?>> classFilter;
    private final AgentFilter<DestInvoke> methodFilter;
    private final AgentFilter<DestInvoke> constructorFilter;

    AbstractInvokeChainFilter(ClassFilter classFilter, InvokeFilter methodFilter, InvokeFilter constructorFilter) {
        this.classFilter = classFilter;
        this.methodFilter = methodFilter;
        this.constructorFilter = constructorFilter;
    }

    @Override
    public boolean accept(InvokeInfo info) {
        Class<?> clazz = info.getInvokeClass();
        if (classFilter == null || classFilter.accept(clazz)) {
            DestInvoke invoke = info.getInvoke();
            return info.isConstructor() ?
                    constructorFilter != null && constructorFilter.accept(invoke) :
                    methodFilter == null || methodFilter.accept(invoke);
        }
        return false;
    }
}
