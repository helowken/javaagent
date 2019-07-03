package agent.server.event;

import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class EventListenerMgr {
    private static final Logger logger = Logger.getLogger(EventListenerMgr.class);
    private static final List<AgentEventListener> listenerList = new ArrayList<>();
    private static final Object listenerLock = new Object();

    public static void reg(AgentEventListener listener) {
        synchronized (listenerLock) {
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            }
        }
    }

    public static void fireEvent(AgentEvent event) {
        List<AgentEventListener> lns;
        synchronized (listenerLock) {
            lns = new ArrayList<>(listenerList);
        }
        lns.forEach(ln -> {
            if (ln.accept(event)) {
                try {
                    ln.onNotify(event);
                } catch (Exception e) {
                    logger.error("Listener {} process event failed.", e, ln);
                }
            }
        });
    }

}
