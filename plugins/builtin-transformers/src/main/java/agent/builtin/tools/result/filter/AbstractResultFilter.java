package agent.builtin.tools.result.filter;

import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.InvokeDescriptorUtils.TextConfig;
import agent.base.utils.Pair;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.ScriptFilter;

import java.util.Map;

public abstract class AbstractResultFilter<T> implements ResultFilter<T> {
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

    public void setClassFilter(AgentFilter<String> classFilter) {
        this.classFilter = classFilter;
    }

    public void setMethodFilter(AgentFilter<String> methodFilter) {
        this.methodFilter = methodFilter;
    }

    public void setConstructorFilter(AgentFilter<String> constructorFilter) {
        this.constructorFilter = constructorFilter;
    }

    public void setScriptFilter(ScriptFilter scriptFilter) {
        this.scriptFilter = scriptFilter;
    }

    @Override
    public boolean accept(Pair<InvokeMetadata, T> pair) {
        InvokeMetadata metadata = pair.left;
        T data = pair.right;
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
                                convertToScriptParamValues(data)
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

    abstract Map<String, Object> convertToScriptParamValues(T value);
}
