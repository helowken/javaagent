package agent.server.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransformResult {
    private final String context;
    private final List<ErrorItem> transformErrorList = new ArrayList<>();
    private final List<ErrorItem> compileErrorList = new ArrayList<>();
    private final List<ErrorItem> reTransformErrorItemList = new ArrayList<>();

    TransformResult(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    void addTransformError(Throwable error, AgentTransformer transformer) {
        this.transformErrorList.add(
                new ErrorItem(null, error, transformer)
        );
    }

    void addReTransformError(Class<?> clazz, Throwable error) {
        this.reTransformErrorItemList.add(
                new ErrorItem(clazz, error, null)
        );
    }

    void addCompileError(Class<?> clazz, Throwable error) {
        this.compileErrorList.add(
                new ErrorItem(clazz, error, null)
        );
    }

    public boolean hasCompileError() {
        return !compileErrorList.isEmpty();
    }

    public List<ErrorItem> getCompileErrorList() {
        return Collections.unmodifiableList(compileErrorList);
    }

    public boolean hasReTransformError() {
        return !reTransformErrorItemList.isEmpty();
    }

    public List<ErrorItem> getReTransformErrorItemList() {
        return Collections.unmodifiableList(reTransformErrorItemList);
    }

    public boolean hasTransformError() {
        return !transformErrorList.isEmpty();
    }

    public List<ErrorItem> getTransformErrorList() {
        return Collections.unmodifiableList(transformErrorList);
    }

    public boolean hasError() {
        return hasCompileError() || hasTransformError() || hasReTransformError();
    }

    public static class ErrorItem {
        private final Class<?> clazz;
        private final Throwable error;
        private final AgentTransformer transformer;

        private ErrorItem(Class<?> clazz, Throwable error, AgentTransformer transformer) {
            this.clazz = clazz;
            this.error = error;
            this.transformer = transformer;
        }

        public String getTargetClassName() {
            return clazz == null ? null : clazz.getName();
        }

        public Throwable getError() {
            return error;
        }

        public AgentTransformer getTransformer() {
            return transformer;
        }
    }
}
