package agent.server.transform;

import agent.base.utils.Utils;

import java.util.List;
import java.util.Map;
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

    public boolean hasError() {
        return !isSuccess();
    }

    public Map<String, Exception> getTransformerToError() {
        return transformContext.transformerList
                .stream()
                .filter(ErrorTraceTransformer::hasError)
                .collect(
                        Collectors.toMap(
                                transformer -> {
                                    if (transformer instanceof ConfigTransformer)
                                        return ((ConfigTransformer) transformer).getRegKey();
                                    return transformer.getClass().getSimpleName();
                                },
                                ErrorTraceTransformer::getError
                        )
                );
    }

    public List<Exception> getTransformerErrorList() {
        return transformContext.transformerList
                .stream()
                .filter(ErrorTraceTransformer::hasError)
                .map(ErrorTraceTransformer::getError)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        if (isSuccess())
            return transformContext.context + " transformed successfully.";
        StringBuilder sb = new StringBuilder(transformContext.context + " transformed failed.\n");
        if (instrumentError != null)
            sb.append("Instrument error: \n")
                    .append(Utils.getErrorStackStrace(instrumentError))
                    .append("\n");
        getTransformerErrorList().forEach(error ->
                sb.append(Utils.getErrorStackStrace(error))
                        .append("\n")
        );
        sb.append("\n");
        return sb.toString();
    }
}
