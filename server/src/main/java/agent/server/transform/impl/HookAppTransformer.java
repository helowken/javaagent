package agent.server.transform.impl;

import agent.base.utils.ReflectionUtils;
import agent.hook.utils.App;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.tools.asm.ProxyCallInfo;
import agent.server.transform.tools.asm.ProxyRegInfo;

import static agent.server.transform.tools.asm.ProxyArgsMask.MASK_INSTANCE;

public class HookAppTransformer extends AbstractConfigTransformer {
    private static final String REG_KEY = "@hookApp";

    @Override
    protected void transformDestInvoke(DestInvoke destInvoke) throws Exception {
        addRegInfo(
                new ProxyRegInfo(destInvoke).addOnReturning(
                        new ProxyCallInfo(
                                null,
                                ReflectionUtils.findFirstMethod(
                                        this.getClass(),
                                        "doHook"
                                ),
                                MASK_INSTANCE,
                                null,
                                getRegKey()
                        )
                )
        );
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    private static void doHook(Object instance) {
        App.instance = instance;
    }
}
