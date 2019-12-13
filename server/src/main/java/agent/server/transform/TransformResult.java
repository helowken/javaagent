package agent.server.transform;

import agent.server.transform.tools.asm.ProxyResult;

import java.util.*;
import java.util.stream.Collectors;

public class TransformResult {
    private final TransformContext transformContext;
    private final List<ErrorItem> transformErrorList = new ArrayList<>();
    private final List<ErrorItem> compileErrorList = new ArrayList<>();
    private final List<ErrorItem> reTransformErrorItemList = new ArrayList<>();
    private final Map<Class<?>, ProxyResult> classToProxyResult = new HashMap<>();

    TransformResult(TransformContext transformContext) {
        this.transformContext = transformContext;
    }

    public TransformContext getTransformContext() {
        return transformContext;
    }

    void addProxyResult(ProxyResult proxyResult) {
        classToProxyResult.put(
                proxyResult.getTargetClass(),
                proxyResult
        );
    }

    Set<Class<?>> getTransformedClassSet() {
        return new HashSet<>(
                classToProxyResult.keySet()
        );
    }

    public byte[] getClassData(Class<?> clazz) {
        return classToProxyResult.get(clazz).getClassData();
    }

    Map<Class<?>, byte[]> getReTransformedClassData() {
        Set<Class<?>> reTransformedClassSet = getTransformedClassSet();
        reTransformErrorItemList.forEach(
                reTransformErrorItem -> reTransformedClassSet.remove(reTransformErrorItem.clazz)
        );
        return reTransformedClassSet.stream()
                .collect(
                        Collectors.toMap(
                                clazz -> clazz,
                                this::getClassData
                        )
                );
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
