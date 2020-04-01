package agent.server.transform;

import agent.base.utils.Utils;
import agent.server.ServerListener;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class InstrumentationMgr implements ServerListener {
    private static final InstrumentationMgr instance = new InstrumentationMgr();
    private Instrumentation instrumentation;

    public static InstrumentationMgr getInstance() {
        return instance;
    }

    private InstrumentationMgr() {
    }

    @Override
    public void onStartup(Object[] args) {
        this.instrumentation = Utils.getArgValue(args, 0);
    }

    @Override
    public void onShutdown() {
    }

    public synchronized Class<?>[] getInitiatedClasses(ClassLoader classLoader) {
        return instrumentation.getInitiatedClasses(classLoader);
    }

    public synchronized Class<?>[] getAllLoadedClasses() {
        return this.instrumentation.getAllLoadedClasses();
    }

    public synchronized void retransform(ClassFileTransformer transformer, Class<?>... classes) throws Throwable {
        if (classes == null || classes.length == 0)
            throw new IllegalArgumentException("No class!");
        try {
            instrumentation.addTransformer(transformer, true);
            instrumentation.retransformClasses(classes);
        } finally {
            instrumentation.removeTransformer(transformer);
        }
    }
}
