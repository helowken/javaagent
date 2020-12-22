package agent.server.transform.search.filter;

import agent.server.transform.search.InvokeChainSearcher.InvokeInfo;

public class InvokeChainSearchFilter extends AbstractInvokeChainFilter {
    private final int maxLevel;

    InvokeChainSearchFilter(ClassFilter classFilter, InvokeFilter methodFilter, InvokeFilter constructorFilter, int maxLevel) {
        super(classFilter, methodFilter, constructorFilter);
        this.maxLevel = maxLevel <= 0 ? 10 : maxLevel;
    }

    public InvokeChainSearchFilter(AbstractInvokeChainFilter filter) {
        this(filter.classFilter, filter.methodFilter, filter.constructorFilter, -1);
    }

    @Override
    public boolean accept(InvokeInfo info) {
        return info.getLevel() < maxLevel &&
                super.accept(info);
    }
}
