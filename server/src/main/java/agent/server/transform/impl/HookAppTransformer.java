package agent.server.transform.impl;

import agent.base.utils.ReflectionUtils;
import agent.hook.utils.App;
import agent.server.transform.TransformContext;
import agent.server.transform.tools.asm.ProxyCallInfo;
import agent.server.transform.tools.asm.ProxyRegInfo;

import static agent.server.transform.tools.asm.ProxyArgsMask.MASK_INSTANCE;

public class HookAppTransformer extends AbstractTransformer {

    @Override
    public void transform(TransformContext transformContext) throws Exception {
        addRegInfo(
                new ProxyRegInfo(
                        ReflectionUtils.findConstructor(
                                transformContext.getTargetClass(),
                                "()V"
                        )
                ).addOnReturning(
                        new ProxyCallInfo(
                                findSelfMethod("doHook"),
                                MASK_INSTANCE
                        )
                )
        );
    }

    private static void doHook(Object instance) {
        App.instance = instance;
    }
}
