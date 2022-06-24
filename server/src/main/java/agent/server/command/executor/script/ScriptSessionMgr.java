package agent.server.command.executor.script;

import agent.server.transform.impl.ScriptEngineMgr;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.Writer;
import java.util.*;

public class ScriptSessionMgr {
    private static final ScriptSessionMgr instance = new ScriptSessionMgr();
    private volatile ScriptEngine engine;
    private Bindings bindings;
    private Writer oldOut;
    private Writer oldErr;
    private final Map<String, ScriptSession> keyToSession = new HashMap<>();
    private final FakeSession fakeSession = new FakeSession();

    public static ScriptSessionMgr getInstance() {
        return instance;
    }

    private ScriptSession getSession(String key) {
        return key == null ?
                fakeSession :
                keyToSession.computeIfAbsent(
                        key,
                        k -> new JsExecSession()
                );
    }

    public synchronized List<String> listSessionKeys() {
        return new ArrayList<>(
                keyToSession.keySet()
        );
    }

    public synchronized void killSession(String key) {
        if (key == null)
            throw new IllegalArgumentException("Session key is null!");
        ScriptSession session = keyToSession.remove(key);
        if (session != null)
            session.destroy();
    }

    private void init() {
        if (engine == null) {
            synchronized (ScriptUtils.class) {
                engine = ScriptEngineMgr.javascript().createEngine();
                bindings = new SimpleScriptContext().getBindings(ScriptContext.ENGINE_SCOPE);
                ScriptContext context = engine.getContext();
                oldOut = context.getWriter();
                oldErr = context.getErrorWriter();
            }
        }
    }

    private void reset() {
        ScriptContext context = engine.getContext();
        bindings.clear();
        context.setWriter(oldOut);
        context.setErrorWriter(oldErr);
    }

    public <T> ScriptExecResult<T> eval(String script, Map<String, Object> pvs) throws Exception {
        fakeSession.setBindings(pvs);
        return eval(fakeSession, script);
    }

    public synchronized <T> ScriptExecResult<T> eval(String key, String script) throws Exception {
        return eval(
                getSession(key),
                script
        );
    }

    private synchronized <T> ScriptExecResult<T> eval(ScriptSession session, String script) throws Exception {
        init();
        session.before();
        ScriptContext context = engine.getContext();
        bindings.putAll(
                session.getBindings()
        );
        context.setWriter(
                session.getWriter()
        );
        context.setErrorWriter(
                session.getErrorWriter()
        );
        try {
            return new ScriptExecResult<>(
                    (T) engine.eval(script, bindings),
                    session.hasContent(),
                    session.getContent(),
                    session.hasError(),
                    session.getErrorContent()
            );
        } finally {
            session.saveBindings(bindings);
            session.after();
            reset();
        }
    }

    private class FakeSession implements ScriptSession {
        private Map<String, Object> bindings;

        private void setBindings(Map<String, Object> bindings) {
            this.bindings = bindings;
        }

        @Override
        public void before() {
            if (bindings == null)
                bindings = Collections.emptyMap();
        }

        @Override
        public void after() {
            bindings = Collections.emptyMap();
        }

        @Override
        public Writer getWriter() {
            return oldOut;
        }

        @Override
        public Writer getErrorWriter() {
            return oldErr;
        }

        @Override
        public Map<String, Object> getBindings() {
            return this.bindings;
        }

        @Override
        public void saveBindings(Map<String, Object> bindings) {
        }

        @Override
        public boolean hasContent() {
            return false;
        }

        @Override
        public boolean hasError() {
            return false;
        }

        @Override
        public String getContent() {
            return null;
        }

        @Override
        public String getErrorContent() {
            return null;
        }

        @Override
        public void destroy() {
        }
    }
}
