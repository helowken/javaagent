package agent.server.command.executor.script;

import agent.server.transform.impl.ScriptEngineMgr;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ScriptUtils {
    private static volatile ScriptEngine engine;
    private static Bindings bindings;

    private static void init() {
        if (engine == null) {
            synchronized (ScriptUtils.class) {
                engine = ScriptEngineMgr.javascript().createEngine();
                bindings = new SimpleScriptContext().getBindings(ScriptContext.ENGINE_SCOPE);
            }
        }
    }

    public static synchronized <T> T eval(String script) throws Exception {
        return eval(
                script,
                Collections.emptyMap()
        );
    }

    public static synchronized <T> T eval(String script, Map<String, Object> pvs) throws Exception {
        init();
        bindings.clear();
        bindings.putAll(pvs);
        return (T) engine.eval(script, bindings);
    }

}
