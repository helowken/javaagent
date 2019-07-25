package agent.server.transform;

import java.util.Map;
import java.util.function.Supplier;

public interface TransformerClassFactory extends Supplier<Map<String, Class<? extends ConfigTransformer>>> {
}
