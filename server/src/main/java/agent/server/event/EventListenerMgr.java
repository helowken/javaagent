package agent.server.event;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventListenerMgr {
    private static final Logger logger = Logger.getLogger(EventListenerMgr.class);
    private static final List<AgentEventListener> listenerList = new ArrayList<>();
    private static final LockObject listenerLock = new LockObject();
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 10,
            5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));

    public static void reg(AgentEventListener listener) {
        listenerLock.sync(lock -> {
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            }
        });
    }

    public static void unreg(AgentEventListener listener) {
        listenerLock.sync(lock -> listenerList.remove(listener));
    }

    public static void fireEvent(AgentEvent event) {
        fireEvent(event, false);
    }

    public static void fireEvent(AgentEvent event, boolean async) {
        List<AgentEventListener> lns = listenerLock.syncValue(lock -> new ArrayList<>(listenerList));
        lns.forEach(ln -> {
            if (ln.accept(event)) {
                if (async)
                    executor.execute(() -> exec(ln, event));
                else
                    exec(ln, event);
            }
        });
    }

    private static void exec(AgentEventListener ln, AgentEvent event) {
        try {
            ln.onNotify(event);
        } catch (Exception e) {
            logger.error("Listener {} process event {} failed.", e, ln, event.getType());
        }
    }
}
