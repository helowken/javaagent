package agent.builtin.tools.result;

import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.InvokeDescriptorUtils.TextConfig;
import agent.base.utils.Pair;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.ScriptFilter;

import java.util.Map;

abstract class ResultFilter<T> implements AgentFilter<Pair<InvokeMetadata, T>> {
    private static final TextConfig textConfig = new TextConfig();

    static {
        textConfig.withReturnType = false;
        textConfig.withPkg = true;
        textConfig.shortForPkgLang = false;
    }

    private AgentFilter<String> classFilter;
    private AgentFilter<String> methodFilter;
    private AgentFilter<String> constructorFilter;
    private ScriptFilter scriptFilter;

    void setClassFilter(AgentFilter<String> classFilter) {
        this.classFilter = classFilter;
    }

    void setMethodFilter(AgentFilter<String> methodFilter) {
        this.methodFilter = methodFilter;
    }

    void setConstructorFilter(AgentFilter<String> constructorFilter) {
        this.constructorFilter = constructorFilter;
    }

    void setScriptFilter(ScriptFilter scriptFilter) {
        this.scriptFilter = scriptFilter;
    }

    @Override
    public boolean accept(Pair<InvokeMetadata, T> pair) {
        InvokeMetadata metadata = pair.left;
        T item = pair.right;
        if (classFilter == null || classFilter.accept(metadata.clazz)) {
            boolean v = isConstructor(metadata.invoke) ?
                    constructorFilter == null || constructorFilter.accept(
                            getInvokeText(metadata.invoke)
                    ) :
                    methodFilter == null || methodFilter.accept(
                            getInvokeText(metadata.invoke)
                    );
            if (v)
                return scriptFilter == null ||
                        scriptFilter.accept(
                                convertTo(item)
                        );
        }
        return false;
    }

    private String getInvokeText(String invoke) {
        return InvokeDescriptorUtils.descToText(invoke, textConfig);
    }

    private boolean isConstructor(String invoke) {
        return invoke.startsWith("(");
    }

    abstract Map<String, Object> convertTo(T value);
}
