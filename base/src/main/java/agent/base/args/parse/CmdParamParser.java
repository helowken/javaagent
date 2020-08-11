package agent.base.args.parse;

public interface CmdParamParser<P> {
    P parse(String[] args);
}
