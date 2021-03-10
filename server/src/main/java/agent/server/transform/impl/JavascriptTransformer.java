package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.invoke.DestInvoke;
import agent.server.transform.exception.InvalidTransformerConfigException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavascriptTransformer extends CallChainTransformer {
    public static final String REG_KEY = "@javascript";
    private static final Logger logger = Logger.getLogger(JavascriptTransformer.class);
    private static final String KEY_CONFIG_SCRIPT = "script";
    private static final String FUNC_ON_BEFORE = "onBefore";
    private static final String FUNC_ON_RETURN = "onReturn";
    private static final String FUNC_ON_THROW_NOT_CATCH = "onThrowNotCatch";
    private static final String FUNC_ON_THROW = "onThrow";
    private static final String FUNC_ON_CATCH = "onCatch";
    private static final String FUNC_ON_AFTER = "onAfter";
    private static final String FUNC_ON_COMPLETE = "onComplete";

    @Override
    protected void doSetConfig(Map<String, Object> config) throws Exception {
        String script = (String) config.get(KEY_CONFIG_SCRIPT);
        if (Utils.isBlank(script))
            throw new InvalidTransformerConfigException("No script found.");
        ScriptEngineMgr.javascript().createEngine(
                getTid(),
                script,
                getTransformerData()
        );
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    @Override
    public void destroy() {
        ScriptEngineMgr.javascript().unreg(
                getTid()
        );
        super.destroy();
    }

    private static class Config extends CallChainConfig<JavascriptItem, JavascriptItem> {

        @Override
        protected JavascriptItem newData(Object[] args, Class<?>[] argTypes, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            JavascriptItem data = new JavascriptItem();
            ignoreError(
                    () -> ScriptEngineMgr.javascript().invoke(instanceKey, FUNC_ON_BEFORE, data, destInvoke, args, argTypes, instanceOrNull),
                    "newData failed."
            );
            return data;
        }

        @Override
        protected JavascriptItem processOnReturning(JavascriptItem data, Object returnValue, Class<?> returnType, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.javascript().invoke(instanceKey, FUNC_ON_RETURN, data, destInvoke, returnValue, returnType, instanceOrNull),
                    "onReturn failed."
            );
            return data;
        }

        @Override
        protected JavascriptItem processOnThrowingNotCatch(JavascriptItem data, Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.javascript().invoke(instanceKey, FUNC_ON_THROW_NOT_CATCH, data, destInvoke, error, instanceOrNull),
                    "onThrowNotCatch failed."
            );
            return data;
        }

        @Override
        protected JavascriptItem processOnThrowing(JavascriptItem data, Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.javascript().invoke(instanceKey, FUNC_ON_THROW, data, destInvoke, error, instanceOrNull),
                    "onThrow failed."
            );
            return data;
        }

        @Override
        protected JavascriptItem processOnCatching(JavascriptItem data, Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.javascript().invoke(instanceKey, FUNC_ON_CATCH, data, destInvoke, error, instanceOrNull),
                    "onCatch failed."
            );
            return data;
        }

        @Override
        protected void processOnAfter(JavascriptItem result, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.javascript().invoke(instanceKey, FUNC_ON_AFTER, result, destInvoke, instanceOrNull),
                    "onAfter failed."
            );
        }

        @Override
        protected void processOnCompleted(List<JavascriptItem> completed, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.javascript().invoke(instanceKey, FUNC_ON_COMPLETE, completed, destInvoke, instanceOrNull),
                    "onComplete failed."
            );
        }
    }

    private static void ignoreError(InvokeFunc func, String errMsg) {
        try {
            func.run();
        } catch (Throwable t) {
            logger.error(errMsg, t);
        }
    }

    public static class JavascriptItem extends InvokeItem {
        public final Map<String, Object> kvs = new HashMap<>();

        @Override
        public String toString() {
            return "{" +
                    "id=" + id +
                    ", parentId=" + parentId +
                    ", invokeId=" + invokeId +
                    ", kvs=" + kvs +
                    '}';
        }
    }

    private interface InvokeFunc {
        void run() throws Throwable;
    }
}
