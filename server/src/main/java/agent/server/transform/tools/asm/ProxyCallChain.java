package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.DEFAULT_AROUND;

public class ProxyCallChain {
    private final ProxyCallSite callSite;
    private final LinkedList<ProxyCall> callList = new LinkedList<>();
    private final Object target;
    private final Object[] args;
    private Object returnValue = null;
    private boolean executed = false;
    private Throwable error;

    ProxyCallChain(ProxyCallSite callSite, Object target, Object[] args) {
        this.callSite = callSite;
        this.target = target;
        this.args = args;
        initCallList();
    }

    private void initCallList() {
        ProxyCallConfig callConfig = callSite.getCallConfig();
        this.callList.addAll(callConfig.getAfterQueue());
        this.callList.addAll(callConfig.getAfterThrowingQueue());
        this.callList.addAll(callConfig.getAfterReturningQueue());
        this.callList.addAll(callConfig.getBeforeQueue());
        this.callList.addAll(callConfig.getAroundQueue());
        this.callList.add(
                newTargetCall()
        );
    }

    private ProxyCall newTargetCall() {
        return Utils.wrapToRtError(
                () -> new ProxyCallAround(
                        new ProxyCallInfo(
                                this,
                                ReflectionUtils.findFirstMethod(
                                        getClass(),
                                        "execTargetMethod"
                                ),
                                DEFAULT_AROUND
                        )
                )
        );
    }

    private List<ProxyCall> copy(Collection<ProxyCall> calls) {
        return new ArrayList<>(calls);
    }

    public Object getTarget() {
        return target;
    }

    public DestInvoke getDestInvoke() {
        return callSite.getCallConfig().getDestInvoke();
    }

    public Object[] getArgs() {
        return args;
    }

    public Class<?>[] getArgTypes() {
        return callSite.getArgTypes();
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public Class<?> getReturnType() {
        return callSite.getReturnType();
    }

    public boolean isExecuted() {
        return executed;
    }

    public Throwable getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }

    public void process() {
        if (!callList.isEmpty())
            callList.removeFirst().run(this);
    }

    void execTargetMethod(ProxyCallChain callChain) {
        try {
            returnValue = callSite.invokeTargetEntity(
                    callChain.getTarget(),
                    callChain.getArgs()
            );
        } catch (Throwable t) {
            error = Utils.getMeaningfulCause(t);
            callSite.formatError(error);
        } finally {
            executed = true;
            callChain.process();
        }
    }

}
