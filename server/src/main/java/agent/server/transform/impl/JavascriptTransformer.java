package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.invoke.DestInvoke;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavascriptTransformer extends CallChainTransformer {
    public static final String REG_KEY = "@javascript";
    private static final Logger logger = Logger.getLogger(JavascriptTransformer.class);
    private static final String ENGINE_NAME = "nashorn";
    private static final String FUNC_ON_BEFORE = "onBefore";
    private static final String FUNC_ON_RETURN = "onReturn";
    private static final String FUNC_ON_THROW = "onError";
    private static final String FUNC_ON_CATCH = "onCatch";
    private static final String FUNC_ON_AFTER = "onAfter";
    private static final String FUNC_ON_COMPLETE = "onComplete";

    @Override
    protected String newLogKey(Map<String, Object> config) {
        return null;
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    private static class Config extends CallChainConfig<JavascriptItem, JavascriptItem> {

        @Override
        public void init() {
            ScriptEngineMgr.reg(instanceKey, ENGINE_NAME);
        }

        @Override
        protected JavascriptItem newData(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs) {
            JavascriptItem data = new JavascriptItem();
            ignoreError(
                    () -> ScriptEngineMgr.invoke(instanceKey, FUNC_ON_BEFORE, data, destInvoke, args, argTypes),
                    "newData failed."
            );
            return data;
        }

        @Override
        protected JavascriptItem processOnReturning(JavascriptItem data, Object returnValue, Class<?> returnType, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.invoke(instanceKey, FUNC_ON_RETURN, data, destInvoke, returnValue, returnType),
                    "onReturn failed."
            );
            return data;
        }

        @Override
        protected JavascriptItem processOnThrowing(JavascriptItem data, Throwable error, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.invoke(instanceKey, FUNC_ON_THROW, data, destInvoke, error),
                    "onThrow failed."
            );
            return data;
        }

        @Override
        protected JavascriptItem processOnCatching(JavascriptItem data, Throwable error, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.invoke(instanceKey, FUNC_ON_CATCH, data, destInvoke, error),
                    "onCatch failed."
            );
            return data;
        }

        @Override
        protected void processOnAfter(JavascriptItem result, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.invoke(instanceKey, FUNC_ON_AFTER, result, destInvoke),
                    "onAfter failed."
            );
        }

        @Override
        protected void processOnCompleted(List<JavascriptItem> completed, DestInvoke destInvoke, Object[] otherArgs) {
            ignoreError(
                    () -> ScriptEngineMgr.invoke(instanceKey, FUNC_ON_COMPLETE, completed, destInvoke),
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
                    "kvs=" + kvs +
                    '}';
        }
    }

    private interface InvokeFunc {
        void run() throws Throwable;
    }
}
