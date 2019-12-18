package agent.server.transform.tools.asm;

import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.*;

abstract class AbstractProxyCall implements ProxyCall {
    private static final Logger logger = Logger.getLogger(AbstractProxyCall.class);
    private static final int MASK_POS_BEFORE = DEFAULT_BEFORE | DEFAULT_METADATA;
    private static final int MASK_POS_ON_RETURNING = DEFAULT_ON_RETURNING | DEFAULT_METADATA;
    private static final int MASK_POS_ON_THROWING = DEFAULT_ON_THROWING | DEFAULT_METADATA;
    private static final int MASK_POS_AFTER = DEFAULT_AFTER | DEFAULT_METADATA;

    private final ProxyPosition position;
    private final ProxyCallInfo callInfo;

    AbstractProxyCall(ProxyPosition position, ProxyCallInfo callInfo) {
        this.position = position;
        this.callInfo = callInfo;
    }

    @Override
    public void run(DestInvoke destInvoke, Object instanceOrNull, Object pv) {
        try {
            callInfo.getProxyMethod().invoke(
                    callInfo.getProxyTarget(),
                    getParams(destInvoke, instanceOrNull, pv)
            );
        } catch (Throwable t) {
            logger.error("Proxy call failed by: {}", t, callInfo);
        }
    }

    private Object[] getParams(DestInvoke destInvoke, Object instanceOrNull, Object pv) {
        int mask = getArgsMask();
        List<Object> params = new ArrayList<>();
        collectParams(params, mask, destInvoke, pv);
        if (useInvokeTarget(mask))
            params.add(instanceOrNull);
        if (useInvokeMethod(mask))
            params.add(
                    destInvoke.getInvokeEntity()
            );
        if (callInfo.hasOtherArgs())
            Collections.addAll(
                    params,
                    callInfo.getOtherArgs()
            );
        return params.toArray();
    }

    abstract void collectParams(List<Object> params, int mask, DestInvoke destInvoke, Object pv);

    private int getArgsMask() {
        int mask = callInfo.getArgsMask();
        switch (position) {
            case BEFORE:
                return MASK_POS_BEFORE & mask;
            case ON_RETURNING:
                return MASK_POS_ON_RETURNING & mask;
            case ON_THROWING:
                return MASK_POS_ON_THROWING & mask;
            case AFTER:
                return MASK_POS_AFTER & mask;
        }
        throw new RuntimeException("Invalid position: " + position);
    }
}
