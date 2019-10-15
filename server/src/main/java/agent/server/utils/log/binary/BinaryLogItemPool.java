package agent.server.utils.log.binary;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BinaryLogItemPool {
    private static final Map<String, Queue<BinaryLogItem>> keyToQueue = new ConcurrentHashMap<>();

    private static Queue<BinaryLogItem> getQueue(String logKey) {
        return keyToQueue.computeIfAbsent(
                logKey,
                key -> new ConcurrentLinkedQueue<>()
        );
    }

    public static BinaryLogItem get(String logKey) {
        BinaryLogItem item = getQueue(logKey).poll();
        return item == null ? new BinaryLogItem() : item;
    }

    public static void put(String logKey, BinaryLogItem item) {
        getQueue(logKey).add(item);
    }

    public static List<BinaryLogItem> getList(String logKey, int maxCount) {
        List<BinaryLogItem> rsList = new LinkedList<>();
        Queue<BinaryLogItem> queue = getQueue(logKey);
        BinaryLogItem item;
        int count = 0;
        while (count < maxCount) {
            item = queue.poll();
            if (item != null) {
                rsList.add(item);
                ++count;
            } else break;
        }
        return rsList;
    }
}
