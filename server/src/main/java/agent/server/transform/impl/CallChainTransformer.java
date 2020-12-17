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
                        getTid()
                };
        }
        return null;
    }

    public abstract static class CallChainConfig<T extends InvokeItem, R> extends ProxyAnnotationConfig<T, R> {

        @Override
        protected T newDataOnBefore(AroundItem<T, R> aroundItem, Object[] args, Class<?>[] argTypes, Object instanceOrNull,
                                    DestInvoke destInvoke, Object[] otherArgs) {
            int invokeId = Utils.getArgValue(otherArgs, 0);
            InvokeItem parentItem = aroundItem.peek();
            T data = newData(args, argTypes, instanceOrNull, destInvoke, otherArgs);
            data.id = aroundItem.nextSeq();
            data.parentId = parentItem == null ? -1 : parentItem.id;
            data.invokeId = invokeId;
            return data;
        }

        @Override
        protected void processOnAfter(R result, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
        }

        @Override
        protected R processOnCatching(T data, Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            return null;
        }

        protected abstract T newData(Object[] args, Class<?>[] argTypes, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs);
    }

    public abstract static class CallChainTimeConfig<T extends InvokeTimeItem, R> extends CallChainConfig<T, R> {
        protected abstract R convertTo(T data);

        @Override
        protected T newDataOnBefore(AroundItem<T, R> aroundItem, Object[] args, Class<?>[] argTypes, Object instanceOrNull,
                                    DestInvoke destInvoke, Object[] otherArgs) {
            long st = System.nanoTime();
            T data = super.newDataOnBefore(aroundItem, args, argTypes, instanceOrNull, destInvoke, otherArgs);
            data.startTime = st;
            return data;
        }

        @Override
        protected R processOnReturning(T data, Object returnValue, Class<?> returnType, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            data.endTime = System.nanoTime();
            return convertTo(data);
        }

        @Override
        protected R processOnThrowing(T data, Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            data.endTime = System.nanoTime();
            data.error = error;
            return convertTo(data);
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
