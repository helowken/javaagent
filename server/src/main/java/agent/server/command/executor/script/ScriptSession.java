package agent.server.command.executor.script;


import java.io.Writer;
import java.util.Map;

public interface ScriptSession {
    void before();

    void after();

    Writer getWriter();

    Writer getErrorWriter();

    Map<String, Object> getBindings();

    void saveBindings(Map<String, Object> bindings);

    boolean hasContent();

    boolean hasError();

    String getContent();

    String getErrorContent();

    void destroy();
}
