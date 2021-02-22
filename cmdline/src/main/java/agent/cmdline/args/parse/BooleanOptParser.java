package agent.cmdline.args.parse;

public class BooleanOptParser extends AbstractOptParser {
    public BooleanOptParser(Object... vs) {
        super(vs);
    }

    @Override
    Object getValue(String arg, ArgList argList, OptConfig optConfig) {
        return true;
    }
}
