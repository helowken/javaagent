package agent.server.command.executor.script;

import java.util.Map;

public class ScriptUtils {

    public static synchronized <T> T eval(String script, Map<String, Object> pvs) throws Exception {
        return ScriptSessionMgr.getInstance().<T>eval(script, pvs).value;
    }

}
