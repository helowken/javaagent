package agent.base.utils;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class ScriptUtils {
    private static final String TYPE_NASHORN = "nashorn";
    private static final String TYPE_JS = "js";
    private static final String TYPE_PYTHON = "python";
    private static final String TYPE_PY = "py";
    private static final Map<String, String> nameToType = new HashMap<>();
    private static final Map<String, ScriptEngine> typeToEngine = new ConcurrentHashMap<>();
    static {
        nameToType.put(TYPE_NASHORN, TYPE_NASHORN);
        nameToType.put(TYPE_JS, TYPE_NASHORN);
        nameToType.put(TYPE_PYTHON, TYPE_PYTHON);
        nameToType.put(TYPE_PY, TYPE_PYTHON);
    }
    private static volatile ScriptEngine engine;
    private static Bindings bindings;

    private static void init() {
        if (engine == null) {
            synchronized (ScriptUtils.class) {
                if (engine == null) {
                    engine = new ScriptEngineManager().getEngineByName("nashorn");
                    if (engine == null)
                        throw new RuntimeException("No nashorn engine found.");
                    bindings = new SimpleScriptContext().getBindings(ScriptContext.ENGINE_SCOPE);
                }
            }
        }
    }

    public static synchronized <T> T eval(String script, Map<String, Object> pvs) throws Exception {
        init();
        bindings.clear();
        bindings.putAll(pvs);
        return (T) engine.eval(script, bindings);
    }

}
