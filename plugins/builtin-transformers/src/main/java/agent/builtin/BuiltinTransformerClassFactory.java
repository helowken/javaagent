package agent.builtin;

import agent.builtin.transformer.CostTimeStatisticsTransformer;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.TransformerClassFactory;

import java.util.HashMap;
import java.util.Map;

public class BuiltinTransformerClassFactory implements TransformerClassFactory {
    private static final Map<String, Class<? extends ConfigTransformer>> keyToTransformerClass = new HashMap<>();

    static {
        keyToTransformerClass.put(CostTimeStatisticsTransformer.REG_KEY, CostTimeStatisticsTransformer.class);
        keyToTransformerClass.put(TraceInvokeTransformer.REG_KEY, TraceInvokeTransformer.class);
    }

    @Override
    public Map<String, Class<? extends ConfigTransformer>> get() {
        return keyToTransformerClass;
    }
}
