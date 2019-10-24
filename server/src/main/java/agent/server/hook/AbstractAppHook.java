package agent.server.hook;

import agent.base.utils.ReflectionUtils;
import agent.hook.plugin.AppHook;
import agent.hook.utils.App;
import agent.hook.utils.AttachType;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;
import agent.server.transform.impl.HookAppTransformer;

import java.util.Collections;

import static agent.server.transform.TransformContext.ACTION_MODIFY;

public abstract class AbstractAppHook implements AppHook {

    protected abstract String getAppClass();

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
        Class<?> appClass = ReflectionUtils.findClass(getAppClass());
        String context = "$hookApp";
        TransformMgr.getInstance().transform(
                Collections.singletonList(
                        new TransformContext(context, appClass, new HookAppTransformer(), ACTION_MODIFY)
                )
        );
    }

    private void dynamicHook() throws Exception {
        App.instance = JvmtiUtils.getInstance()
                .findObjectByClassName(
                        getAppClass()
                );
    }
}
