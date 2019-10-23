package agent.server.transform;

import java.util.*;
import java.util.stream.Collectors;

public class TransformResult {
    private final TransformContext transformContext;
    private final List<ErrorItem> transformErrorList = new ArrayList<>();
    private final List<ErrorItem> compileErrorList = new ArrayList<>();
    private final List<ErrorItem> reTransformErrorItemList = new ArrayList<>();
    private final Map<Class<?>, byte[]> classToData = new HashMap<>();

    TransformResult(TransformContext transformContext) {
        this.transformContext = transformContext;
    }

    public TransformContext getTransformContext() {
        return transformContext;
    }

    Set<Class<?>> getTransformedClassSet() {
        return new HashSet<>(
                classToData.keySet()
        );
    }

    private Set<Class<?>> getReTransformedClassSet() {
        Set<Class<?>> reTransformedClassSet = getTransformedClassSet();
        reTransformErrorItemList.forEach(
                reTransformErrorItem -> reTransformedClassSet.remove(reTransformErrorItem.clazz)
        );
        return reTransformedClassSet;
    }

    void saveClassData(Class<?> clazz, byte[] data) {
        classToData.put(clazz, data);
    }

    public byte[] getClassData(Class<?> clazz) {
        return classToData.get(clazz);
    }

    Map<Class<?>, byte[]> getReTransformedClassData() {
        return getReTransformedClassSet()
                .stream()
                .collect(
                        Collectors.toMap(
                                clazz -> clazz,
                                this::getClassData
                        )
                );
    }

    void addTransformError(Class<?> clazz, Throwable error, AgentTransformer transformer) {
        this.transformErrorList.add(
                new ErrorItem(clazz, error, transformer)
        );
    }

    void addReTransformError(Class<?> clazz, Throwable error) {
        error.printStackTrace();
        this.reTransformErrorItemList.add(
                new ErrorItem(clazz, error)
        );
    }

    void addCompileError(Class<?> clazz, Throwable error) {
        this.compileErrorList.add(
                new ErrorItem(clazz, error)
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
        public final Class<?> clazz;
        public final Throwable error;
        public final AgentTransformer transformer;

        private ErrorItem(Class<?> clazz, Throwable error) {
            this(clazz, error, null);
        }

        private ErrorItem(Class<?> clazz, Throwable error, AgentTransformer transformer) {
            this.clazz = clazz;
            this.error = error;
            this.transformer = transformer;
        }
    }
}
