package agent.server.transform;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.server.ServerListener;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.ResetClassEvent;
import agent.server.event.impl.TransformClassEvent;
import agent.server.transform.impl.ResetClassTransformer;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static agent.server.transform.TransformContext.ACTION_MODIFY;
import static agent.server.transform.TransformContext.ACTION_RESET;

public class ResetClassMgr implements ServerListener, AgentEventListener {
    private static final Logger logger = Logger.getLogger(ResetClassMgr.class);
    private static final LockObject classLock = new LockObject();
    private static ResetClassMgr instance = new ResetClassMgr();
    private Map<String, Set<Class<?>>> contextToTransformedClassSet = new HashMap<>();

    public static ResetClassMgr getInstance() {
        return instance;
    }

    private ResetClassMgr() {
    }

    @Override
    public void onNotify(AgentEvent event) {
        TransformClassEvent tcEvent = (TransformClassEvent) event;
        final int action = tcEvent.getAction();
        switch (action) {
            case ACTION_MODIFY:
                addToCache(
                        tcEvent.getContext(),
                        tcEvent.getTransformedClassSet()
                );
                break;
            case ACTION_RESET:
                removeFromCache(
                        tcEvent.getContext(),
                        tcEvent.getTransformedClassSet()
                );
                break;
            default:
                throw new RuntimeException("Invalid action: " + action);
        }
    }

    private void addToCache(String context, Set<Class<?>> classSet) {
        classLock.sync(lock ->
                contextToTransformedClassSet.computeIfAbsent(
                        context,
                        key -> new HashSet<>()
                ).addAll(classSet)
        );
    }

    private void removeFromCache(String context, Set<Class<?>> classSet) {
        boolean allReset = classLock.syncValue(lock -> {
            Set<Class<?>> transformedClassSet = contextToTransformedClassSet.get(context);
            if (transformedClassSet != null) {
                transformedClassSet.removeAll(classSet);
                if (transformedClassSet.isEmpty())
                    contextToTransformedClassSet.remove(context);
            }
            return contextToTransformedClassSet.isEmpty();
        });
        EventListenerMgr.fireEvent(
                new ResetClassEvent(context, allReset)
        );
    }

    private List<TransformContext> newResetContexts(String contextExpr, Set<String> classExprSet) {
        logger.debug("Reset context expr: {}, class expr set: {}", contextExpr, classExprSet);
        Pattern contextPattern = contextExpr == null ? null : Pattern.compile(contextExpr);
        List<Pattern> classPatterns = classExprSet == null || classExprSet.isEmpty() ?
                null :
                classExprSet.stream()
                        .map(Pattern::compile)
                        .collect(Collectors.toList());
        List<TransformContext> transformContextList = new ArrayList<>();
        classLock.sync(lock ->
                contextToTransformedClassSet.forEach((context, classSet) -> {
                    if (contextPattern == null || contextPattern.matcher(context).matches()) {
                        Set<Class<?>> resetClassSet = new HashSet<>();
                        classSet.forEach(clazz -> {
                            if (classPatterns == null ||
                                    classPatterns.stream().anyMatch(
                                            classPattern -> classPattern.matcher(clazz.getName()).matches())
                                    ) {
                                logger.debug("Add to reset, context: {}, class: {}", context, clazz);
                                resetClassSet.add(clazz);
                            }
                        });
                        transformContextList.add(
                                new TransformContext(
                                        context,
                                        resetClassSet,
                                        Collections.singletonList(
                                                new ResetClassTransformer()
                                        ),
                                        ACTION_RESET
                                )
                        );
                    }
                })
        );
        return transformContextList;
    }

    public List<TransformResult> resetAllClasses() {
        return resetClasses(null, null);
    }

    public List<TransformResult> resetClasses(String contextExpr, Set<String> classExprSet) {
        List<TransformContext> transformContextList = newResetContexts(contextExpr, classExprSet);
        if (transformContextList.isEmpty()) {
            logger.debug("No class need to reset.");
            return Collections.emptyList();
        } else {
            return TransformMgr.getInstance().transform(transformContextList);
        }
    }

    public Map<String, Set<Class<?>>> getContextToTransformedClassSet() {
        return classLock.syncValue(lock -> {
            Map<String, Set<Class<?>>> rsMap = new HashMap<>();
            contextToTransformedClassSet.forEach((context, classSet) ->
                    rsMap.put(context, new HashSet<>(classSet))
            );
            return rsMap;
        });
    }

    @Override
    public void onStartup(Object[] args) {
        EventListenerMgr.reg(TransformClassEvent.class, instance);
    }

    @Override
    public void onShutdown() {
        resetAllClasses();
    }
}
