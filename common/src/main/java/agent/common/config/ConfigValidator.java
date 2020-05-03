package agent.common.config;

import java.util.Collection;
import java.util.regex.Pattern;

import static agent.base.utils.AssertUtils.fail;

public class ConfigValidator {
    private static final Pattern classPattern = Pattern.compile("[a-zA-Z0-9_.*#\\[$]+");
    private static final Pattern invokePattern = Pattern.compile("[a-zA-Z0-9_.*<>() ]+");

    public static void validateClassFilters(Collection<String> includes, Collection<String> excludes) {
        validateFilters(includes, excludes, classPattern);
    }

    public static void validateInvokeFilters(Collection<String> includes, Collection<String> excludes) {
        validateFilters(includes, excludes, invokePattern);
    }

    private static void validateFilters(Collection<String> includes, Collection<String> excludes, Pattern pattern) {
        validateFilters(includes, "Invalid include: ", pattern);
        validateFilters(excludes, "Invalid exclude: ", pattern);
    }

    private static void validateAnyNotEmpty(String errMsg, Collection... vs) {
        for (Collection v : vs) {
            if (v != null && !v.isEmpty())
                return;
        }
        fail(errMsg);
    }

    private static void validateFilters(Collection<String> filters, String errMsg, Pattern pattern) {
        if (filters != null)
            filters.forEach(
                    filter -> {
                        if (!pattern.matcher(filter).matches())
                            throw new RuntimeException(errMsg + filter);
                    }
            );
    }
}
