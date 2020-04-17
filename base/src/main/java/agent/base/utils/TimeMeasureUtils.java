package agent.base.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public class TimeMeasureUtils {
    private static final Logger logger = Logger.getLogger(TimeMeasureUtils.class);

    public static void run(Utils.WithoutValueFunc func, String pattern, Object... pvs) {
        run(func, null, pattern, pvs);
    }

    public static void run(Utils.WithoutValueFunc func, Consumer<Throwable> errorHandler, String pattern, Object... pvs) {
        run(
                () -> {
                    func.run();
                    return null;
                },
                e -> {
                    if (errorHandler != null)
                        errorHandler.accept(e);
                    else
                        logger.error("Run failed.", e);
                    return null;
                },
                pattern,
                pvs
        );
    }

    public static <T> T run(Utils.WithValueFunc<T> func, String pattern, Object... pvs) {
        return run(func, null, pattern, pvs);
    }

    public static <T> T run(Utils.WithValueFunc<T> func, Function<Throwable, T> errorHandler, String pattern, Object... pvs) {
        Object[] args;
        if (pvs != null) {
            args = new Object[pvs.length + 1];
            System.arraycopy(pvs, 0, args, 0, pvs.length);
        } else
            args = new Object[1];

        long st = System.currentTimeMillis();
        try {
            return func.run();
        } catch (Throwable e) {
            if (errorHandler != null)
                return errorHandler.apply(e);
            throw new RuntimeException(e);
        } finally {
            long et = System.currentTimeMillis();
            args[args.length - 1] = et - st;
            logger.debug(pattern, args);
        }
    }
}
