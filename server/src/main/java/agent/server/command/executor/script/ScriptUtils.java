package agent.server.command.executor.script;

import agent.server.transform.impl.ScriptEngineMgr;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.Writer;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ScriptUtils {
    private static volatile ScriptEngine engine;
    private static Bindings bindings;

    private static void init() {
        if (engine == null) {
            synchronized (ScriptUtils.class) {
                engine = ScriptEngineMgr.javascript().createEngine();
                bindings =  new SimpleScriptContext().getBindings(ScriptContext.ENGINE_SCOPE);
            }
        }
    }

    public static synchronized <T> T eval(String script, Map<String, Object> pvs, Writer out, Writer err) throws Exception {
        init();
        if (pvs != null)
            bindings.putAll(pvs);
        ScriptContext context = engine.getContext();
        Writer oldOut = context.getWriter();
        Writer oldErr = context.getErrorWriter();
        if (out != null)
            context.setWriter(out);
        if (err != null)
            context.setErrorWriter(err);
        try {
            return (T) engine.eval(script, bindings);
        } finally {
            bindings.clear();
            context.setWriter(oldOut);
            context.setErrorWriter(oldErr);
        }
    }

}
