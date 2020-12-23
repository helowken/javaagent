package agent.server.transform.search.filter;

import agent.server.transform.search.InvokeChainSearcher.InvokeInfo;

public class InvokeChainSearchFilter extends AbstractInvokeChainFilter {
    private static final int DEFAULT_MAX_LEVEL = 10;
    private final int maxLevel;

    InvokeChainSearchFilter(ClassFilter classFilter, InvokeFilter methodFilter, InvokeFilter constructorFilter, int maxLevel) {
        super(classFilter, methodFilter, constructorFilter);
        this.maxLevel = maxLevel <= 0 ? DEFAULT_MAX_LEVEL : maxLevel;
    }

    public InvokeChainSearchFilter(AbstractInvokeChainFilter filter) {
        this(filter.classFilter, filter.methodFilter, filter.constructorFilter, -1);
    }

    @Override
    public boolean accept(InvokeInfo info) {
        return info.getLevel() < maxLevel && super.accept(info);
    }

    @Override
    public String toString() {
        return "InvokeChainSearchFilter{" +
                "maxLevel=" + maxLevel +
                ", classFilter=" + classFilter +
                ", methodFilter=" + methodFilter +
                ", constructorFilter=" + constructorFilter +
                '}';
    }
}
