package agent.builtin;

import agent.builtin.transformer.CostTimeMeasureTransformer;
import agent.builtin.transformer.CostTimeStatisticsTransformer;
import agent.builtin.transformer.SaveClassDataTransformer;
import agent.builtin.transformer.TraceInvokeTransformer;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.TransformerClassFactory;

import java.util.HashMap;
import java.util.Map;

public class BuiltinTransformerClassFactory implements TransformerClassFactory {
    private static final Map<String, Class<? extends ConfigTransformer>> keyToTransformerClass = new HashMap<>();

    static {
        keyToTransformerClass.put(CostTimeMeasureTransformer.REG_KEY, CostTimeMeasureTransformer.class);
        keyToTransformerClass.put(CostTimeStatisticsTransformer.REG_KEY, CostTimeStatisticsTransformer.class);
        keyToTransformerClass.put(TraceInvokeTransformer.REG_KEY, TraceInvokeTransformer.class);
        keyToTransformerClass.put(SaveClassDataTransformer.REG_KEY, SaveClassDataTransformer.class);
    }

    @Override
    public Map<String, Class<? extends ConfigTransformer>> get() {
        return keyToTransformerClass;
    }
}
