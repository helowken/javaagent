package agent.server.transform.search.filter;

public class InvokeChainMatchFilter extends AbstractInvokeChainFilter {

    InvokeChainMatchFilter(ClassFilter classFilter, InvokeFilter methodFilter, InvokeFilter constructorFilter) {
        super(classFilter, methodFilter, constructorFilter);
    }

    @Override
    public String toString() {
        return "InvokeChainMatchFilter{" +
                "classFilter=" + classFilter +
                ", methodFilter=" + methodFilter +
                ", constructorFilter=" + constructorFilter +
                '}';
    }
}
