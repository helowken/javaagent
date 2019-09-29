package agent.server.transform.impl.dynamic;

import agent.base.utils.Logger;
import agent.base.utils.Pair;
import agent.hook.plugin.ClassFinder;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.impl.AdditionalTransformEvent;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;

import java.util.*;

public class AdditionalTransformListener implements AgentEventListener {
    private static final Logger logger = Logger.getLogger(AdditionalTransformListener.class);
    private final List<AdditionalTransformEvent> eventList = new LinkedList<>();

    @Override
    public void onNotify(AgentEvent event) {
        eventList.add((AdditionalTransformEvent) event);
    }

    public List<TransformContext> getContextList() {
        logger.debug("Event list: {}", eventList);
        ClassFinder classFinder = TransformMgr.getInstance().getClassFinder();
        Map<String, Pair<Set<Class<?>>, Map<String, Pair<ClassLoader, byte[]>>>> contextToPair = new HashMap<>();
        for (AdditionalTransformEvent event : eventList) {
            String context = event.getContext();
            Pair<Set<Class<?>>, Map<String, Pair<ClassLoader, byte[]>>> p = contextToPair.computeIfAbsent(
                    context,
                    key -> new Pair<>(new HashSet<>(), new HashMap<>())
            );
            event.getClassNameToBytes().forEach(
                    (className, bs) -> {
                        Class<?> clazz = classFinder.findClass(context, className);
                        p.left.add(clazz);
                        p.right.put(
                                className,
                                new Pair<>(clazz.getClassLoader(), bs)
                        );
                    }
            );
        }
        List<TransformContext> transformContextList = new ArrayList<>();
        contextToPair.forEach((context, pair) ->
                transformContextList.add(
                        new TransformContext(
                                context,
                                pair.left,
                                Collections.singletonList(
                                        new AdditionalClassTransformer(pair.right)
                                ),
                                false
                        )
                )
        );
        logger.debug("Transform context list: {}", transformContextList);
        return transformContextList;
    }

    @Override
    public boolean accept(AgentEvent event) {
        return AdditionalTransformEvent.EVENT_TYPE.equals(event.getType());
    }
}
