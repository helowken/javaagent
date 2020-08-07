package agent.builtin.tools.result;

public interface ResultHandler<P> {
    void exec(P params) throws Exception;
}
