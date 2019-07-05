package agent.server.utils.log.text;

import agent.base.utils.StringParser;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.AbstractLogWriter;

import java.util.Map;

@SuppressWarnings("unchecked")
public class TextLogWriter extends AbstractLogWriter<TextLogConfig, String> {
    private StringParser.CompiledStringExpr expr;

    TextLogWriter(TextLogConfig logConfig) {
        super(logConfig);
        expr = StringParser.compile(logConfig.getOutputFormat());
    }

    @Override
    protected String convertContent(Object v) {
        Map<String, Object> paramValues = (Map) v;
        return expr.eval(paramValues, (pvs, key) -> ParamValueUtils.formatValue(pvs, key, logConfig.getTimeFormat())) + "\n";
    }

    @Override
    protected int computeSize(String content) {
        return content.length();
    }
}
