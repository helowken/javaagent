package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.common.utils.Registry;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ScriptEngineMgr {
    private static final Logger logger = Logger.getLogger(ScriptEngineMgr.class);
    private static final Registry<String, ScriptEngine> registry = new Registry<>();
    private static final ScriptEngineManager manager = new ScriptEngineManager();

    public static void reg(String key, String engineName) {
        registry.regIfAbsent(
                key,
                k -> manager.getEngineByName(engineName)
        );
    }

    public static void unreg(String key) {
        registry.unreg(key);
    }

    public static void eval(String key, String script) throws Exception {
        registry.get(key).eval(script);
    }

    public static void invoke(String key, String funcName, Object... args) throws Exception {
        ScriptEngine engine = registry.get(key);
        Object value = engine.get(funcName);
        if (value instanceof ScriptObjectMirror) {
            ScriptObjectMirror som = (ScriptObjectMirror) value;
            if (som.isFunction()) {
                ((Invocable) engine).invokeFunction(funcName, args);
                return;
            }
        }
        logger.debug("No script function found by name: " + funcName + ", key: " + key);
    }
}
