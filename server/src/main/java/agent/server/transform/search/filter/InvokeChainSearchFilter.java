package agent.server.transform.search.filter;

import agent.server.transform.search.InvokeChainSearcher.InvokeInfo;

public class InvokeChainSearchFilter extends AbstractInvokeChainFilter {
    private final int maxLevel;

    InvokeChainSearchFilter(ClassFilter classFilter, InvokeFilter methodFilter, InvokeFilter constructorFilter, int maxLevel) {
        super(classFilter, methodFilter, constructorFilter);
        this.maxLevel = maxLevel <= 0 ? 100 : maxLevel;
    }

    @Override
    public boolean accept(InvokeInfo info) {
        return info.getLevel() <= maxLevel &&
                super.accept(info);
    }
}
