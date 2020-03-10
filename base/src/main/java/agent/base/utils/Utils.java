package agent.base.utils;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class Utils {
    public static String sUuid() {
        return UUID.randomUUID().toString();
    }

    public static void wrapToRtError(WithoutValueFunc func) {
        wrapToRtError(func, null);
    }

    public static void wrapToRtError(WithoutValueFunc func, Supplier<String> errMsgSupplier) {
        try {
            func.run();
        } catch (Exception e) {
            throw toRtError(e, errMsgSupplier);
        }
    }

    public static <T> T wrapToRtError(WithValueFunc<T> func) {
        return wrapToRtError(func, null);
    }

    public static <T> T wrapToRtError(WithValueFunc<T> func, Supplier<String> errMsgSupplier) {
        try {
            return func.run();
        } catch (Throwable e) {
            throw toRtError(e, errMsgSupplier);
        }
    }

    public static RuntimeException toRtError(Throwable e, Supplier<String> errMsgSupplier) {
        if (e instanceof RuntimeException)
            return (RuntimeException) e;
        return errMsgSupplier == null ?
                new RuntimeException(e) :
                new RuntimeException(errMsgSupplier.get(), e);
    }

    public static int parseInt(String s, String name) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new RuntimeException(name + " is not a int string.");
        }
    }

    public static long parseLong(String s, String name) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new RuntimeException(name + " is not a long string.");
        }
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    public static <T> T getArgValue(Object[] args, int idx) {
        if (args == null || args.length <= idx)
            throw new IllegalArgumentException("Invalid argument index: " + idx);
        return castValue(args[idx], "Invalid argument type.");
    }

    public static <T> T castValue(Object value, String errMsg) {
        try {
            return (T) value;
        } catch (Exception e) {
            throw new RuntimeException(errMsg);
        }
    }

    public static <T> T getConfigValue(Map<String, Object> config, String key) {
        return getConfigValue(config, key, null);
    }

    public static <T> T getConfigValue(Map<String, Object> config, String key, Map<String, Object> defaultValueMap) {
        Object value = config.get(key);
        if (value == null && defaultValueMap != null)
            value = defaultValueMap.get(key);
        return castValue(
                value,
                "Invalid value type for key: " + key
        );
    }

    public static String getErrorStackStrace(Throwable t) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(out)) {
            t.printStackTrace(ps);
            return new String(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMergedErrorMessage(Throwable t) {
        StringBuilder sb = new StringBuilder();
        getErrorMessages(t).forEach(errMsg -> sb.append(errMsg).append("\n"));
        return sb.toString();
    }

    public static List<String> getErrorMessages(Throwable t) {
        List<String> errMsgList = new ArrayList<>();
        while (t != null) {
            String msg = t.getMessage();
            if (!isBlank(msg)) {
                errMsgList.add(msg);
            }
            t = t.getCause();
        }
        return errMsgList;
    }

    public static Properties loadProperties(String path) throws Exception {
        try (InputStream in = new FileInputStream(path)) {
            Properties props = new Properties();
            props.load(in);
            return props;
        }
    }

    public static <T> boolean isEmpty(Collection<T> colls) {
        return colls == null || colls.isEmpty();
    }

    public static <T> boolean nonEmpty(Collection<T> colls) {
        return !isEmpty(colls);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static String blankToNull(String s) {
        if (s != null && s.trim().isEmpty())
            return null;
        return s;
    }

    public static <T> Collection<T> emptyToNull(Collection<T> vs, Predicate<T> filter) {
        if (vs == null)
            return null;
        Collection<T> rs = vs.stream().filter(filter).collect(Collectors.toList());
        return rs.isEmpty() ? null : rs;
    }

    public static Set<String> splitToSet(String s, String sep) {
        if (s == null)
            return Collections.emptySet();
        return Stream.of(s.split(sep))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toSet());
    }

    public static String[] splitToArray(String s, String sep) {
        return splitToSet(s, sep).toArray(new String[0]);
    }

    public static <T> T firstValidValue(String errMsg, Function<T, Boolean> checkFunc, T... vs) {
        if (vs != null) {
            for (T v : vs) {
                if (checkFunc.apply(v))
                    return v;
            }
        }
        if (errMsg != null)
            throw new IllegalArgumentException(errMsg);
        return null;
    }

    public static <T> String join(String sep, T... vs) {
        if (vs == null)
            return null;
        if (vs.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vs.length; ++i) {
            if (i > 0)
                sb.append(sep);
            sb.append(vs[i]);
        }
        return sb.toString();
    }

    public static <T> T firstNotNull(String errMsg, T... vs) {
        return firstValidValue(errMsg, Objects::nonNull, vs);
    }

    public static String firstNotBlank(String errMsg, String... vs) {
        return firstValidValue(errMsg, Utils::isNotBlank, vs);
    }

    public static Throwable getMeaningfulCause(Throwable t) {
        if (t instanceof InvocationTargetException && t.getCause() != null)
            return getMeaningfulCause(t.getCause());
        return t;
    }

    public static <T> boolean contains(T[] vs, T v) {
        for (T t : vs) {
            if (t.equals(v))
                return true;
        }
        return false;
    }

    public static <T> List<T> reverse(List<T> vs) {
        Collections.reverse(vs);
        return vs;
    }

    public static <T> T[] reverse(T[] vs) {
        T[] rs = (T[]) Array.newInstance(
                vs.getClass().getComponentType(),
                vs.length
        );
        for (int i = 0, j = vs.length - 1; i < vs.length; ++i, --j) {
            rs[i] = vs[j];
        }
        return rs;
    }

    public interface WithValueFunc<T> {
        T run() throws Exception;
    }

    public interface WithoutValueFunc {
        void run() throws Exception;
    }
}
