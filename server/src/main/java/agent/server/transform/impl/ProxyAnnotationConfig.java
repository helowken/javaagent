package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.tools.asm.annotation.OnAfter;
import agent.server.transform.tools.asm.annotation.OnBefore;
import agent.server.transform.tools.asm.annotation.OnReturning;
import agent.server.transform.tools.asm.annotation.OnThrowing;

import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.*;

public abstract class ProxyAnnotationConfig<T, R> {
    public static final int ARGS_NONE = -1;
    public static final int ARGS_ON_BEFORE = 1;
    public static final int ARGS_ON_RETURNING = 2;
    public static final int ARGS_ON_THROWING = 3;
    public static final int ARGS_ON_AFTER = 4;
    private static final Logger logger = Logger.getLogger(ProxyAnnotationConfig.class);

    private final ThreadLocal<AroundItem<T, R>> local = new ThreadLocal<>();

    @OnBefore(mask = DEFAULT_BEFORE | MASK_INVOKE, argsHint = ARGS_ON_BEFORE)
    public void onBefore(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object... otherArgs) {
        AroundItem<T, R> currAroundItem = local.get();
        if (currAroundItem == null) {
            currAroundItem = new AroundItem<>();
            local.set(currAroundItem);
        }
        T data = newDataOnBefore(args, argTypes, destInvoke, otherArgs);
        if (data != null)
            currAroundItem.add(data);
    }

    @OnReturning(mask = DEFAULT_ON_RETURNING | MASK_INVOKE, argsHint = ARGS_ON_RETURNING)
    public void onReturning(Object returnValue, Class<?> returnType, DestInvoke destInvoke, Object... otherArgs) {
        AroundItem<T, R> currAroundItem = getAroundItem("returning", destInvoke);
        if (currAroundItem != null)
            currAroundItem.complete(
                    data -> processOnReturning(data, returnValue, returnType, destInvoke, otherArgs),
                    isAddResultToLast(false)
            );
    }

    @OnThrowing(mask = DEFAULT_ON_THROWING | MASK_INVOKE, argsHint = ARGS_ON_THROWING)
    public void onThrowing(Throwable error, DestInvoke destInvoke, Object... otherArgs) {
        AroundItem<T, R> currAroundItem = getAroundItem("throwing", destInvoke);
        if (currAroundItem != null)
            currAroundItem.complete(
                    data -> processOnThrowing(data, error, destInvoke, otherArgs),
                    isAddResultToLast(true)
            );
    }

    @OnAfter(mask = MASK_INVOKE, argsHint = ARGS_ON_AFTER)
    private void onAfter(DestInvoke destInvoke, Object... args) {
        AroundItem<T, R> currAroundItem = getAroundItem("after", destInvoke);
        if (currAroundItem != null) {
            processOnAfter(destInvoke, args);
            if (currAroundItem.isCompleted()) {
                local.remove();
                processOnCompleted(currAroundItem.getCompleted(), destInvoke, args);
            }
        }
    }

    protected boolean isAddResultToLast(boolean error) {
        return false;
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

    protected abstract T newDataOnBefore(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs);

    protected abstract R processOnReturning(T data, Object returnValue, Class<?> returnType, DestInvoke destInvoke, Object[] otherArgs);

    protected abstract R processOnThrowing(T data, Throwable error, DestInvoke destInvoke, Object[] otherArgs);

    protected abstract void processOnAfter(DestInvoke destInvoke, Object[] otherArgs);

    protected abstract void processOnCompleted(List<R> completed, DestInvoke destInvoke, Object[] otherArgs);

}
