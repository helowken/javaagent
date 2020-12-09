package agent.server.transform.impl;

import agent.server.command.executor.script.ExportFuncs;
import agent.server.transform.TransformerData;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ScriptEngineMgr {
    private static final ScriptEngineMgr jsEngineMgr = new ScriptEngineMgr("nashorn");
    private static final String KEY_EXPORT_FUNCS = "$";
    private static final String KEY_DATA = "$d";
    private final Map<String, ScriptEngine> keyToEngine = new ConcurrentHashMap<>();
    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final String engineName;

    public static ScriptEngineMgr javascript() {
        return jsEngineMgr;
    }

    private ScriptEngineMgr(String engineName) {
        this.engineName = engineName;
        this.manager.getBindings().putAll(
                getNativeGlobalBindings()
        );
    }

    public void unreg(String key) {
        keyToEngine.remove(key);
    }

    public ScriptEngine createEngine() {
        ScriptEngine engine = manager.getEngineByName(engineName);
        if (engine == null)
            throw new RuntimeException("Engine can not be created by name: " + engineName);
        return engine;
    }

    public void setGlobalBindings(String script) throws Exception {
        ScriptEngine engine = createEngine();
        engine.eval(script);
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        lock.writeLock().lock();
        try {
            Bindings globalBindings = manager.getBindings();
            globalBindings.clear();
            globalBindings.putAll(bindings);
            globalBindings.putAll(
                    getNativeGlobalBindings()
            );
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Map<String, Object> getNativeGlobalBindings() {
        return Collections.singletonMap(KEY_EXPORT_FUNCS, ExportFuncs.instance);
    }

    private ScriptEngine getEngine(String key) {
        ScriptEngine engine = keyToEngine.get(key);
        if (engine == null)
            throw new RuntimeException("No engine found by key: " + key);
        return engine;
    }

    public void createEngine(String key, String script, TransformerData transformerData) throws Exception {
        ScriptEngine engine = createEngine();
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put(KEY_DATA, transformerData);
        engine.eval(script);
        keyToEngine.put(key, engine);
    }

    public void invoke(String key, String funcName, Object... args) throws Exception {
        ScriptEngine engine = getEngine(key);
        lock.readLock().lock();
        try {
            Object value = engine.get(funcName);
            if (value instanceof ScriptObjectMirror) {
                ScriptObjectMirror som = (ScriptObjectMirror) value;
                if (som.isFunction()) {
                    ((Invocable) engine).invokeFunction(funcName, args);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, String> getGlobalBindings() {
        lock.readLock().lock();
        try {
            return bindingsToMap(
                    manager.getBindings()
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, String> getEngineBindings(String key) {
        return bindingsToMap(
                getEngine(key).getBindings(ScriptContext.ENGINE_SCOPE)
        );
    }

    private Map<String, String> bindingsToMap(Bindings bindings) {
        Map<String, String> rsMap = new HashMap<>(
        );
        bindings.forEach(
                (k, v) -> rsMap.put(
                        k,
                        String.valueOf(v)
                )
        );
        return rsMap;
    }
}
