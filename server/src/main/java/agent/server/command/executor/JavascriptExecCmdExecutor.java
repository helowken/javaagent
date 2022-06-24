package agent.server.command.executor;

import agent.base.utils.Utils;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.DefaultExecResult;
import agent.cmdline.command.result.ExecResult;
import agent.common.config.JsExec;
import agent.server.command.executor.script.ScriptExecResult;
import agent.server.command.executor.script.ScriptSessionMgr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class JavascriptExecCmdExecutor extends AbstractCmdExecutor {
    @Override
    protected ExecResult doExec(Command cmd) throws Exception {
        JsExec jse = cmd.getContent();
        String script = jse.getScript();
        Map<String, Object> rsMap = new HashMap<>();
        ScriptExecResult<Object> result = null;
        if (Utils.isNotBlank(script)) {
            result = ScriptSessionMgr.getInstance().eval(
                    jse.getSessionId(),
                    script
            );
            if (result.hasContent)
                rsMap.put("Standard Output", result.content);
            if (result.hasError)
                rsMap.put("Standard Error", result.errorContent);
        }
        rsMap.put(
                "Value",
                result == null ? "null" : format(result.value)
        );
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                null,
                rsMap
        );
    }

    @SuppressWarnings("unchecked")
    private Object format(Object o) {
        if (o == null)
            return null;
        if (o instanceof Collection)
            return ((Collection) o).stream()
                    .map(this::format)
                    .collect(
                            Collectors.toList()
                    );
        else if (o instanceof Map) {
            Map<Object, Object> rsMap = new HashMap<>();
            ((Map) o).forEach(
                    (key, value) -> rsMap.put(
                            format(key),
                            format(value)
                    )
            );
            return rsMap;
        }
        return o.toString();
    }
}
