package agent.server.transform.exception;

import agent.server.transform.ErrorTraceTransformer;
import agent.base.utils.Utils;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class MultipleTransformException extends Exception {
    private final Exception transformError;
    private final Map<String, List<ErrorTraceTransformer>> contextToTransformers;

    public MultipleTransformException(String errMsg, Exception transformError, Map<String, List<ErrorTraceTransformer>> contextToTransformers) {
        super(errMsg);
        this.transformError = transformError;
        this.contextToTransformers = contextToTransformers;
    }

    public void printStackTrace(PrintStream s) {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage()).append("\n");
        if (transformError != null)
            sb.append("===== Transform Inner Error: \n").append(Utils.getErrorStackStrace(transformError)).append("\n\n");
        contextToTransformers.forEach((context, transformerList) -> {
            sb.append("===== Context: ").append(context).append("\n");
            int count = 0;
            for (ErrorTraceTransformer transformer : transformerList) {
                if (count > 0)
                    sb.append("\n");
                sb.append("===== Transformer: ").append(transformer.getClass().getName()).append("\n");
                sb.append(Utils.getErrorStackStrace(transformer.getError())).append("\n");
                ++count;
            }
            sb.append("\n");
        });
        s.println(sb.toString());
    }


}
