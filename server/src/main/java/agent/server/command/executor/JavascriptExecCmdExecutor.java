package agent.server.command.executor;

import agent.base.utils.ScriptUtils;
import agent.base.utils.Utils;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.DefaultExecResult;
import agent.server.command.executor.script.ExportFuncs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class JavascriptExecCmdExecutor extends AbstractCmdExecutor {
    private static final String KEY_EXPORT_FUNCS = "$";

    @Override
    ExecResult doExec(Command cmd) throws Exception {
        String script = cmd.getContent();
        Object rs = null;
        if (Utils.isNotBlank(script)) {
            rs = ScriptUtils.eval(
                    script,
                    Collections.singletonMap(
                            KEY_EXPORT_FUNCS,
                            ExportFuncs.instance
                    )
            );
        }
        if (rs == null)
            rs = "null";
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                null,
                format(rs)
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
