package agent.common.config;

import agent.base.struct.annotation.PojoProperty;

public class JsExec extends AbstractValidConfig {
    @PojoProperty(index = 0)
    private String sessionId;
    @PojoProperty(index = 1)
    private String script;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public void validate() {
        validateNotNull(script, "Script");
    }
}
