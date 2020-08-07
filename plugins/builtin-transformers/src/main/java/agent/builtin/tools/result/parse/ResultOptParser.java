package agent.builtin.tools.result.parse;

public interface ResultOptParser<P> {
    P parse(String[] args);
}
