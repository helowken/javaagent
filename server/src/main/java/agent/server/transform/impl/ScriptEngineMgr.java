package agent.server.transform.impl;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ScriptEngineMgr {
    private static final ScriptEngineMgr jsEngineMgr = new ScriptEngineMgr("nashorn");
    private final Map<String, ScriptEngine> keyToEngine = new ConcurrentHashMap<>();
    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final String engineName;

    public static ScriptEngineMgr javascript() {
        return jsEngineMgr;
    }

    private ScriptEngineMgr(String engineName) {
        this.engineName = engineName;
    }

    public void unreg(String key) {
        keyToEngine.remove(key);
    }

    private ScriptEngine createEngine() {
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
            manager.getBindings().clear();
            manager.getBindings().putAll(bindings);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ScriptEngine getEngine(String key) {
        ScriptEngine engine = keyToEngine.get(key);
        if (engine == null)
            throw new RuntimeException("No engine found by key: " + key);
        return engine;
    }

    public void setEngineBindings(String key, String script) throws Exception {
        ScriptEngine engine = createEngine();
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
}
