package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.invoke.DestInvoke;
import agent.server.transform.tools.asm.annotation.*;

import java.util.List;

import static agent.invoke.proxy.ProxyArgsMask.*;

public abstract class ProxyAnnotationConfig<T, R> {
    public static final int ARGS_NONE = -1;
    public static final int ARGS_ON_BEFORE = 1;
    public static final int ARGS_ON_RETURNING = 2;
    public static final int ARGS_ON_THROWING = 3;
    public static final int ARGS_ON_CATCHING = 4;
    public static final int ARGS_ON_AFTER = 5;
    private static final Logger logger = Logger.getLogger(ProxyAnnotationConfig.class);
    private final static Object dummy = new Object();

    private final ThreadLocal<AroundItem<T, R>> local = new ThreadLocal<>();
    private final ThreadLocal<Object> banningLocal = new ThreadLocal<>();
    protected String instanceKey;

    void setInstanceKey(String key) {
        this.instanceKey = key;
    }

    private boolean isBanning() {
        return banningLocal.get() != null;
    }

    private void setBanning(boolean v) {
        if (v)
            banningLocal.set(dummy);
        else
            banningLocal.remove();
    }

    @OnBefore(mask = DEFAULT_BEFORE | DEFAULT_METADATA, argsHint = ARGS_ON_BEFORE)
    public void onBefore(Object[] args, Class<?>[] argTypes, Object instanceOrNull, DestInvoke destInvoke, Object... otherArgs) {
        if (isBanning())
            return;
        AroundItem<T, R> currAroundItem = local.get();
        if (currAroundItem == null) {
            currAroundItem = new AroundItem<>();
            local.set(currAroundItem);
        }
        T data = newDataOnBefore(args, argTypes, instanceOrNull, destInvoke, otherArgs);
        if (data != null)
            currAroundItem.push(data);
    }

    @OnReturning(mask = DEFAULT_ON_RETURNING | DEFAULT_METADATA, argsHint = ARGS_ON_RETURNING)
    public void onReturning(Object returnValue, Class<?> returnType, Object instanceOrNull, DestInvoke destInvoke, Object... otherArgs) {
        if (isBanning())
            return;
        AroundItem<T, R> currAroundItem = getAroundItem("returning", destInvoke);
        if (currAroundItem != null)
            currAroundItem.complete(
                    data -> processOnReturning(data, returnValue, returnType, instanceOrNull, destInvoke, otherArgs),
                    false
            );
    }

    @OnCatching(mask = DEFAULT_ON_CATCHING | DEFAULT_METADATA, argsHint = ARGS_ON_CATCHING)
    public void onCatching(Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object... otherArgs) {
        if (isBanning())
            return;
        AroundItem<T, R> currAroundItem = getAroundItem("catching", destInvoke);
        if (currAroundItem != null)
            currAroundItem.complete(
                    data -> processOnCatching(data, error, instanceOrNull, destInvoke, otherArgs),
                    true
            );
    }

    @OnThrowing(mask = DEFAULT_ON_THROWING | DEFAULT_METADATA, argsHint = ARGS_ON_THROWING)
    public void onThrowing(Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object... otherArgs) {
        if (isBanning())
            return;
        AroundItem<T, R> currAroundItem = getAroundItem("throwing", destInvoke);
        if (currAroundItem != null)
            currAroundItem.complete(
                    data -> processOnThrowing(data, error, instanceOrNull, destInvoke, otherArgs),
                    false
            );
    }

    @OnAfter(mask = DEFAULT_METADATA, argsHint = ARGS_ON_AFTER)
    private void onAfter(Object instanceOrNull, DestInvoke destInvoke, Object... args) {
        if (isBanning())
            return;
        AroundItem<T, R> currAroundItem = getAroundItem("after", destInvoke);
        if (currAroundItem != null) {
            processOnAfter(currAroundItem.peekResult(), instanceOrNull, destInvoke, args);
            if (currAroundItem.isCompleted()) {
                local.remove();
                try {
                    setBanning(true);
                    processOnCompleted(currAroundItem.getCompleted(), instanceOrNull, destInvoke, args);
                } finally {
                    setBanning(false);
                }
            }
        }
    }

    private AroundItem<T, R> getAroundItem(String stage, DestInvoke destInvoke) {
        AroundItem<T, R> currAroundItem = local.get();
        if (currAroundItem == null)
            logger.error("No node found on {} for dest invoke: {}", stage, destInvoke);
        return currAroundItem;
    }

    protected AroundItem<T, R> getAroundItem() {
        return local.get();
    }

    protected abstract T newDataOnBefore(Object[] args, Class<?>[] argTypes, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs);

    protected abstract R processOnReturning(T data, Object returnValue, Class<?> returnType, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs);

    protected abstract R processOnCatching(T data, Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs);

    protected abstract R processOnThrowing(T data, Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs);

    protected abstract void processOnAfter(R result, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs);

    protected abstract void processOnCompleted(List<R> completed, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs);

}
