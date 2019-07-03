package agent.base.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeFormatUtils {
    private static final Map<String, TimeItem> patternToTimeItem = new HashMap<>();
    private static final LockObject patternLock = new LockObject();

    public static String format(String pattern, long timeMillis) {
        return patternLock.syncValue(lock ->
                patternToTimeItem.computeIfAbsent(pattern, TimeItem::new)
        ).format(timeMillis);
    }

    private static class TimeItem {
        private final DateFormat df;
        private final Date date = new Date();
        private final LockObject lock = new LockObject();

        private TimeItem(String pattern) {
            this.df = new SimpleDateFormat(pattern);
        }

        String format(long timeMillis) {
            return lock.syncValue(lo -> {
                date.setTime(timeMillis);
                return df.format(date);
            });
        }
    }
}
