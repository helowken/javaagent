package agent.server.utils;

import javax.script.*;

public class ExportUtils {

    public static void main(String[] args) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        System.out.println(manager.getEngineByName("jython"));
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("jython");
        Bindings bindings = new SimpleScriptContext().getBindings(ScriptContext.ENGINE_SCOPE);
        String script = "print(111)";
        engine.eval(script, bindings);
    }
}
