package agent.server.transform.tools.asm;

import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.*;

abstract class AbstractProxyCall implements ProxyCall {
    private static final Logger logger = Logger.getLogger(AbstractProxyCall.class);
    private static final int MASK_POS_BEFORE = MASK_ARGS | MASK_ARG_TYPES | MASK_INVOKE_TARGET;
    private static final int MASK_POS_AFTER_RETURNING = MASK_POS_BEFORE | MASK_RETURN_VALUE | MASK_RETURN_TYPE;
    private static final int MASK_POS_AFTER_THROWING = MASK_POS_BEFORE | MASK_ERROR;
    private static final int MASK_POS_AFTER = MASK_POS_AFTER_RETURNING | MASK_POS_AFTER_THROWING;
    private static final int MASK_POS_AROUND = MASK_POS_BEFORE | MASK_PROXY_CHAIN;

    private final ProxyPosition position;
    private final ProxyCallInfo callInfo;

    AbstractProxyCall(ProxyPosition position, ProxyCallInfo callInfo) {
        this.position = position;
        this.callInfo = callInfo;
    }

    protected void exec(ProxyCallChain callChain) {
        try {
            callInfo.getProxyMethod().invoke(
                    callInfo.getProxyTarget(),
                    getParams(callChain)
            );
        } catch (Throwable t) {
            logger.error("Proxy call failed by: {}", t, callInfo);
        }
    }

    private Object[] getParams(ProxyCallChain callChain) {
        int mask = getArgsMask();
        List<Object> params = new ArrayList<>();
        if (useArgs(mask))
            params.add(
                    callChain.getArgs()
            );
        if (useArgTypes(mask))
            params.add(
                    callChain.getArgTypes()
            );
        if (useReturnValue(mask))
            params.add(
                    callChain.getReturnValue()
            );
        if (useReturnType(mask))
            params.add(
                    callChain.getReturnType()
            );
        if (useError(mask))
            params.add(
                    callChain.getError()
            );
        if (useProxyChain(mask))
            params.add(callChain);
        if (useInvokeTarget(mask))
            params.add(
                    callChain.getTarget()
            );
        if (useInvokeMethod(mask))
            params.add(
                    callChain.getDestInvoke().getSourceEntity()
            );
        if (callInfo.hasOtherArgs())
            Collections.addAll(
                    params,
                    callInfo.getOtherArgs()
            );
        return params.toArray();
    }

    private int getArgsMask() {
        int mask = callInfo.getArgsMask();
        switch (position) {
            case BEFORE:
                return MASK_POS_BEFORE & mask;
            case AFTER:
                return MASK_POS_AFTER & mask;
            case AROUND:
                return MASK_POS_AROUND & mask;
            case AFTER_RETURNING:
                return MASK_POS_AFTER_RETURNING & mask;
            case AFTER_THROWING:
                return MASK_POS_AFTER_THROWING & mask;
        }
        throw new RuntimeException("Invalid position: " + position);
    }
}
