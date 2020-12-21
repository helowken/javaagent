package agent.server.transform.search.filter;

import agent.base.utils.Logger;
import agent.server.command.executor.script.ScriptUtils;

import java.util.Map;

public class ScriptFilter implements AgentFilter<Map<String, Object>> {
    private static final Logger logger = Logger.getLogger(ScriptFilter.class);
    private final String script;

    public ScriptFilter(String script) {
        this.script = script;
    }

    @Override
    public boolean accept(Map<String, Object> pvs) {
        try {
            return ScriptUtils.eval(script, pvs, null, null);
        } catch (Exception e) {
            logger.error("Eval script failed: {}, paramValues: {}", e, script, pvs);
        }
        return false;
    }
}
