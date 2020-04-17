package agent.server.transform;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.server.ServerListener;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.TransformClassEvent;

import java.util.*;

import static agent.server.transform.TransformContext.ACTION_MODIFY;
import static agent.server.transform.TransformContext.ACTION_RESET;

public class ResetMgr implements ServerListener, AgentEventListener {
    private static final Logger logger = Logger.getLogger(ResetMgr.class);
    private static final LockObject classLock = new LockObject();
    private static ResetMgr instance = new ResetMgr();
    private Set<Class<?>> transformedClassSet = new HashSet<>();

    public static ResetMgr getInstance() {
        return instance;
    }

    private ResetMgr() {
    }

    @Override
    public void onNotify(AgentEvent event) {
        TransformClassEvent tcEvent = (TransformClassEvent) event;
        final int action = tcEvent.getAction();
        switch (action) {
            case ACTION_MODIFY:
                addToCache(
                        tcEvent.getTransformedClassSet()
                );
                break;
            case ACTION_RESET:
                removeFromCache(
                        tcEvent.getTransformedClassSet()
                );
                break;
            default:
                throw new RuntimeException("Invalid action: " + action);
        }
    }

    private void addToCache(Set<Class<?>> classSet) {
        classLock.sync(
                lock -> transformedClassSet.addAll(classSet)
        );
    }

    private void removeFromCache(Set<Class<?>> classSet) {
//        boolean allReset = classLock.syncValue(lock -> {
//            Set<Class<?>> transformedClassSet = contextToTransformedClassSet.get(context);
//            if (transformedClassSet != null) {
//                transformedClassSet.removeAll(classSet);
//                if (transformedClassSet.isEmpty())
//                    contextToTransformedClassSet.remove(context);
//            }
//            return contextToTransformedClassSet.isEmpty();
//        });
//        EventListenerMgr.fireEvent(
//                new ResetEvent(context, allReset)
//        );
    }

    private List<TransformContext> newResetContexts(String contextExpr, Set<String> classExprSet) {
//        logger.debug("Reset context expr: {}, class expr set: {}", contextExpr, classExprSet);
//        Pattern contextPattern = contextExpr == null ? null : Pattern.compile(contextExpr);
//        List<Pattern> classPatterns = classExprSet == null || classExprSet.isEmpty() ?
//                null :
//                classExprSet.stream()
//                        .map(Pattern::compile)
//                        .collect(Collectors.toList());
//        List<TransformContext> transformContextList = new ArrayList<>();
//        classLock.sync(lock ->
//                contextToTransformedClassSet.forEach((context, classSet) -> {
//                    if (contextPattern == null || contextPattern.matcher(context).matches()) {
//                        Set<Class<?>> resetClassSet = new HashSet<>();
//                        classSet.forEach(clazz -> {
//                            if (classPatterns == null ||
//                                    classPatterns.stream().anyMatch(
//                                            classPattern -> classPattern.matcher(clazz.getName()).matches())
//                                    ) {
//                                logger.debug("Add to reset, context: {}, class: {}", context, clazz);
//                                resetClassSet.add(clazz);
//                            }
//                        });
//                        transformContextList.add(
//                                new TransformContext(
//                                        context,
//                                        resetClassSet,
//                                        Collections.singletonList(
//                                                new ResetTransformer()
//                                        ),
//                                        ACTION_RESET
//                                )
//                        );
//                    }
//                })
//        );
//        return transformContextList;
        return null;
    }

    public TransformResult resetAllClasses() {
        return resetClasses(null, null);
    }

    public TransformResult resetClasses(String contextExpr, Set<String> classExprSet) {
//        List<TransformContext> transformContextList = newResetContexts(contextExpr, classExprSet);
//        if (transformContextList.isEmpty()) {
//            logger.debug("No class need to reset.");
//            return Collections.emptyList();
//        } else {
//            return TransformMgr.getInstance().transform(transformContextList);
//        }
        return null;
    }

    public Map<String, Set<Class<?>>> getContextToTransformedClassSet() {
        return classLock.syncValue(lock -> {
            Map<String, Set<Class<?>>> rsMap = new HashMap<>();
//            contextToTransformedClassSet.forEach((context, classSet) ->
//                    rsMap.put(context, new HashSet<>(classSet))
//            );
            return rsMap;
        });
    }

    @Override
    public void onStartup(Object[] args) {
        EventListenerMgr.reg(TransformClassEvent.class, this);
    }

    @Override
    public void onShutdown() {
        resetAllClasses();
    }
}
