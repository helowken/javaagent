package agent.server.transform.impl;

import agent.base.utils.Utils;
import agent.invoke.DestInvoke;

import java.lang.reflect.Method;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_ON_AFTER;
import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_ON_BEFORE;

public abstract class CallChainTransformer extends ProxyAnnotationConfigTransformer {

    @Override
    protected Object[] newOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint) {
        switch (argsHint) {
            case ARGS_ON_BEFORE:
                return new Object[]{
                        DestInvokeIdRegistry.getInstance().get(destInvoke)
                };
            case ARGS_ON_AFTER:
                return new Object[]{
                        logKey
                };
        }
        return null;
    }

    public abstract static class CallChainConfig<T extends InvokeItem> extends ProxyAnnotationConfig<T, T> {

        @Override
        protected T newDataOnBefore(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs) {
            int invokeId = Utils.getArgValue(otherArgs, 0);
            AroundItem<T, T> aroundItem = getAroundItem();
            InvokeItem parentItem = aroundItem.peek();
            T data = newData(args, argTypes, destInvoke, otherArgs);
            data.id = aroundItem.nextSeq();
            data.parentId = parentItem == null ? -1 : parentItem.id;
            data.invokeId = invokeId;
            return data;
        }

        @Override
        protected void processOnAfter(DestInvoke destInvoke, Object[] otherArgs) {
        }

        protected abstract T newData(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs);
    }

    public abstract static class CallChainTimeConfig<T extends InvokeTimeItem> extends CallChainConfig<T> {

        @Override
        protected T newDataOnBefore(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs) {
            long st = System.nanoTime();
            T data = super.newDataOnBefore(args, argTypes, destInvoke, otherArgs);
            data.startTime = st;
            return data;
        }

        @Override
        protected T processOnReturning(T data, Object returnValue, Class<?> returnType, DestInvoke destInvoke, Object[] otherArgs) {
            data.endTime = System.nanoTime();
            return data;
        }

        @Override
        protected T processOnThrowing(T data, Throwable error, DestInvoke destInvoke, Object[] otherArgs) {
            data.endTime = System.nanoTime();
            data.error = error;
            return data;
        }
    }

    public static abstract class InvokeItem {
        public int id;
        public int parentId;
        public int invokeId;
    }

    public static class InvokeTimeItem extends InvokeItem {
        public long startTime;
        public long endTime;
        public Throwable error;
    }
}
