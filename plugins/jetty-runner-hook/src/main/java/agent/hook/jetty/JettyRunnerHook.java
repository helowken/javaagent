package agent.hook.jetty;

import agent.base.utils.ReflectionUtils;
import agent.hook.jetty.transformer.HookJettyRunnerTransformer;
import agent.hook.plugin.AppHook;
import agent.hook.utils.App;
import agent.hook.utils.AttachType;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;

class JettyRunnerHook implements AppHook {
    private static final String JETTY_RUNNER_CLASS = "org.eclipse.jetty.runner.Runner";

    @Override
    public void hook(AttachType attachType) throws Exception {
        if (AttachType.STATIC.equals(attachType))
            staticHook();
        else if (AttachType.DYNAMIC.equals(attachType))
            dynamicHook();
        else
            throw new RuntimeException("Unsupported attach type: " + attachType);
    }

    private void staticHook() throws Exception {
        Class<?> jettyRunnerClass = ReflectionUtils.findClass(JETTY_RUNNER_CLASS);
        String context = "hookJettyRunner";
        TransformMgr.getInstance().transform(
                new TransformContext(
                        context,
                        jettyRunnerClass,
                        new HookJettyRunnerTransformer(jettyRunnerClass),
                        true
                )
        );
    }

    private void dynamicHook() {
        App.instance = JvmtiUtils.getInstance().findObjectByClassName(JETTY_RUNNER_CLASS);
    }
}
