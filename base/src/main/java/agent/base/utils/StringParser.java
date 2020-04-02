package agent.base.utils;


import agent.base.exception.StringParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StringParser {
    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";
    private static final int PREFIX_LEN = PREFIX.length();
    private static final int SUFFIX_LEN = SUFFIX.length();

    public static String getKey(String key) {
        return PREFIX + key + SUFFIX;
    }

    public static String eval(String pattern, Map<String, Object> paramValues) {
        return eval(pattern, paramValues, null);
    }

    public static String eval(String pattern, Map<String, Object> paramValues, ParamValueFormatter func) {
        return compile(pattern).eval(paramValues, func);
    }

    public static CompiledStringExpr compile(String pattern) {
        int patternLen = pattern.length();
        CompiledStringExpr expr = new CompiledStringExpr();
        int start = 0;
        while (start < patternLen) {
            int pos = pattern.indexOf(PREFIX, start);
            if (pos > -1) {
                if (pos > start)
                    expr.add(pattern.substring(start, pos));
                int end = pattern.indexOf(SUFFIX, pos + PREFIX_LEN);
                if (end > -1) {
                    String key = pattern.substring(pos + PREFIX_LEN, end).trim();
                    expr.add(key, true);
                    start = end + SUFFIX_LEN;
                } else {
                    expr.add(pattern.substring(pos));
                    break;
                }
            } else {
                expr.add(pattern.substring(start));
                break;
            }
        }
        return expr;
    }

    public static class CompiledStringExpr {
        private final List<ExprItem> itemList = new ArrayList<>();

        private void add(String content) {
            this.add(content, false);
        }

        private void add(String content, boolean isKey) {
            itemList.add(new ExprItem(content, isKey));
        }

        public List<ExprItem> getKeys() {
            return itemList.stream().filter(item -> item.isKey).collect(Collectors.toList());
        }

        public String eval(Map<String, Object> paramValues) {
            return eval(paramValues, null);
        }

        public String eval(Map<String, Object> paramValues, ParamValueFormatter func) {
            StringBuilder sb = new StringBuilder();
            itemList.forEach(item -> {
                if (item.isKey) {
                    Object value = func != null ?
                            func.format(paramValues, item.content)
                            : paramValues.get(item.content);
                    if (value == null)
                        throw new StringParseException("No value found by key: \"" + item.content + "\"");
                    sb.append(value);
                } else
                    sb.append(item.content);
            });
            return sb.toString();
        }
    }

    public static class ExprItem {
        final String content;
        final boolean isKey;

        private ExprItem(String content, boolean isKey) {
            this.content = content;
            this.isKey = isKey;
        }

        public String getContent() {
            return content;
        }
    }

    public interface ParamValueFormatter {
        Object format(Map<String, Object> paramValues, String key);
    }
}
