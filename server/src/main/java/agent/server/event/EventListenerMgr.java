package agent.server.event;

import agent.base.utils.Constants;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventListenerMgr {
    private static final Logger logger = Logger.getLogger(EventListenerMgr.class);
    private static final Map<Class<? extends AgentEvent>, List<AgentEventListener>> eventToListenerList = new HashMap<>();
    private static final LockObject listenerLock = new LockObject();
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,
            10,
            5,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(100),
            Utils.newThreadFactory("Event")
    );

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread(executor::shutdown, Constants.AGENT_THREAD_PREFIX + "Event-shutdown")
        );
    }

    public static void reg(Class<? extends AgentEvent> eventType, AgentEventListener listener) {
        listenerLock.sync(lock -> {
            List<AgentEventListener> listenerList = eventToListenerList.computeIfAbsent(
                    eventType,
                    key -> new ArrayList<>()
            );
            if (!listenerList.contains(listener))
                listenerList.add(listener);
        });
    }

    public static void unreg(Class<? extends AgentEvent> eventType, AgentEventListener listener) {
        listenerLock.sync(lock -> {
            List<AgentEventListener> listenerList = eventToListenerList.get(eventType);
            if (listenerList != null) {
                listenerList.remove(listener);
                if (listenerList.isEmpty())
                    eventToListenerList.remove(eventType);
            }
        });
    }

    public static void fireEvent(AgentEvent event) {
        fireEvent(event, false);
    }

    public static void fireEvent(AgentEvent event, boolean async) {
        List<AgentEventListener> lns = listenerLock.syncValue(
                lock -> {
                    List<AgentEventListener> listenerList = eventToListenerList.get(event.getClass());
                    if (listenerList == null)
                        return null;
                    return new ArrayList<>(listenerList);
                }
        );
        if (lns != null) {
            lns.forEach(ln -> {
                if (async)
                    executor.execute(() -> exec(ln, event));
                else
                    exec(ln, event);
            });
        }
    }

    private static void exec(AgentEventListener ln, AgentEvent event) {
        try {
            ln.onNotify(event);
        } catch (Exception e) {
            logger.error("Listener {} process event {} failed.", e, ln, event.getClass().getName());
        }
    }
}
