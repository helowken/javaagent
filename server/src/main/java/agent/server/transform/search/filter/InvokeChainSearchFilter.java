package agent.server.transform.search.filter;

import agent.server.transform.search.InvokeChainSearcher.InvokeInfo;

public class InvokeChainSearchFilter extends AbstractInvokeChainFilter {
    private final int maxLevel;

    InvokeChainSearchFilter(ClassFilter classFilter, InvokeFilter methodFilter, InvokeFilter constructorFilter, int maxLevel) {
        super(classFilter, methodFilter, constructorFilter);
        if (maxLevel <= 0)
            throw new IllegalArgumentException("Max search level must be > 0.");
        this.maxLevel = maxLevel;
    }

    @Override
    public boolean accept(InvokeInfo info) {
        return info.getLevel() <= maxLevel &&
                super.accept(info);
    }
}
