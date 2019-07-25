package agent.hook.plugin;

import agent.hook.utils.AttachType;

public interface AppHook {
    void hook(AttachType attachType) throws Exception;
}
