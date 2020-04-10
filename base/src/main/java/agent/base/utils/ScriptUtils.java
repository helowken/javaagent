package agent.base.utils;

import javax.script.*;
import java.util.Map;

public class ScriptUtils {
    private static volatile ScriptEngine engine;
    private static Bindings bindings;

    private static void init() {
        if (engine == null) {
            synchronized (ScriptUtils.class) {
                if (engine == null) {
                    engine = new ScriptEngineManager().getEngineByName("nashorn");
                    if (engine == null)
                        throw new RuntimeException("No nashorn engine found.");
                    ScriptContext context = new SimpleScriptContext();
                    bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> T eval(String script, Map<String, Object> pvs) throws Exception {
        init();
        bindings.clear();
        bindings.putAll(pvs);
        return (T) engine.eval(script, bindings);
    }


}