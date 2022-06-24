package agent.common.config;

public class JsConfig {
    public static final int BINDINGS_GLOBAL = 1;
    public static final int BINDINGS_ENGINE = 2;
    public static final int BINDINGS_ALL = 3;
    public static final int ACTION_GET = 0;
    public static final int ACTION_SET = 1;
    public static final int ACTION_LIST = 2;

    private int action;
    private int bindings;
    private String script;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getBindings() {
        return bindings;
    }

    public void setBindings(int bindings) {
        this.bindings = bindings;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
