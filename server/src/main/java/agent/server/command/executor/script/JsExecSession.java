package agent.server.command.executor.script;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class JsExecSession implements ScriptSession {
    private final Map<String, Object> bindings = new HashMap<>();
    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();

    @Override
    public void before() {
        out = new StringWriter();
        err = new StringWriter();
    }

    @Override
    public void after() {
        out = null;
        err = null;
    }

    @Override
    public Writer getWriter() {
        return out;
    }

    @Override
    public Writer getErrorWriter() {
        return err;
    }

    @Override
    public Map<String, Object> getBindings() {
        return bindings;
    }

    @Override
    public void saveBindings(Map<String, Object> bindings) {
        this.bindings.putAll(bindings);
    }

    @Override
    public boolean hasContent() {
        return hasWriterContent(out);
    }

    @Override
    public boolean hasError() {
        return hasWriterContent(err);
    }

    @Override
    public String getContent() {
        return getWriterContent(out);
    }

    @Override
    public String getErrorContent() {
        return getWriterContent(err);
    }

    @Override
    public void destroy() {
        bindings.clear();
        out = null;
        err = null;
    }

    private boolean hasWriterContent(StringWriter writer) {
        return writer.getBuffer().length() > 0;
    }

    private String getWriterContent(StringWriter writer) {
        return writer.toString();
    }
}
