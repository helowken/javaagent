package agent.server.transform.impl;

import agent.base.utils.ReflectionUtils;
import agent.hook.utils.App;
import agent.server.transform.TransformContext;
import agent.server.transform.tools.asm.ProxyCallInfo;
import agent.server.transform.tools.asm.ProxyRegInfo;

import static agent.server.transform.tools.asm.ProxyArgsMask.MASK_INVOKE_TARGET;

public class HookAppTransformer extends AbstractTransformer {

    @Override
    public void transform(TransformContext transformContext) throws Exception {
        addRegInfo(
                new ProxyRegInfo(
                        ReflectionUtils.findConstructor(
                                transformContext.getTargetClass(),
                                "()V"
                        )
                ).addAfter(
                        new ProxyCallInfo(
                                findSelfMethod("doHook"),
                                MASK_INVOKE_TARGET
                        )
                )
        );
//        CtClass ctClass = new AgentClassPool().get(transformContext.getFirstTargetClass().getName());
//        CtConstructor constructor = ctClass.getDeclaredConstructor(new CtClass[0]);
//        constructor.insertAfter(App.class.getName() + ".instance = this;");
    }

    private static void doHook(Object instance) {
        App.instance = instance;
    }
}
