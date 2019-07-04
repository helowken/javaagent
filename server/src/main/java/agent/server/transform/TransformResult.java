package agent.server.transform;

import java.util.List;
import java.util.stream.Collectors;

public class TransformResult {
    public final TransformContext transformContext;
    public final Exception instrumentError;

    public TransformResult(TransformContext transformContext, Exception instrumentError) {
        this.transformContext = transformContext;
        this.instrumentError = instrumentError;
    }

    public boolean isSuccess() {
        return instrumentError == null &&
                transformContext.transformerList.stream()
                        .noneMatch(ErrorTraceTransformer::hasError);
    }

    public List<Exception> getTransformerErrorList() {
        return transformContext.transformerList.stream()
                .filter(ErrorTraceTransformer::hasError)
                .map(ErrorTraceTransformer::getError)
                .collect(Collectors.toList());
    }
}
