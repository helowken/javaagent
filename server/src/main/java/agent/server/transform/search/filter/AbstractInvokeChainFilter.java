package agent.server.transform.search.filter;

import agent.server.transform.search.InvokeChainSearcher.InvokeInfo;
import agent.server.transform.tools.asm.AsmUtils;

abstract class AbstractInvokeChainFilter implements AgentFilter<InvokeInfo> {
    final ClassFilter classFilter;
    final InvokeFilter methodFilter;
    final InvokeFilter constructorFilter;

    AbstractInvokeChainFilter(ClassFilter classFilter, InvokeFilter methodFilter, InvokeFilter constructorFilter) {
        this.classFilter = classFilter;
        this.methodFilter = methodFilter;
        this.constructorFilter = constructorFilter;
    }

    @Override
    public boolean accept(InvokeInfo info) {
        Class<?> clazz = info.getInvokeClass();
        if (classFilter == null || classFilter.accept(clazz)) {
            return info.isConstructor() ?
                    constructorFilter != null && constructorFilter.accept(
                            getInvokeFullName(info)
                    ) :
                    methodFilter == null || methodFilter.accept(
                            getInvokeFullName(info)
                    );
        }
        return false;
    }

    private String getInvokeFullName(InvokeInfo info) {
        return AsmUtils.getInvokeFullName(
                info.getInvokeName(),
                info.getInvokeDesc()
        );
    }
}
