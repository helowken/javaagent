package agent.base.utils;


import agent.base.exception.StringParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StringParser {
    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";

    public static String eval(String pattern, Map<String, Object> paramValues) {
        return eval(pattern, paramValues, null);
    }

    public static String eval(String pattern, Map<String, Object> paramValues, ParamValueFormatter func) {
        return compile(pattern).eval(paramValues, func);
    }

    public static CompiledStringExpr compile(String pattern) {
        return compile(pattern, PREFIX, SUFFIX);
    }

    public static CompiledStringExpr compile(String pattern, String prefix, String suffix) {
        int prefixLen = prefix.length();
        int suffixLen = suffix.length();
        int patternLen = pattern.length();
        CompiledStringExpr expr = new CompiledStringExpr();
        int start = 0;
        while (start < patternLen) {
            int pos = pattern.indexOf(prefix, start);
            if (pos > -1) {
                if (pos > start)
                    expr.add(pattern.substring(start, pos));
                int end = pattern.indexOf(suffix, pos + prefixLen);
                if (end > -1) {
                    String key = pattern.substring(pos + prefixLen, end).trim();
                    expr.add(key, true);
                    start = end + suffixLen;
                } else {
                    String s = pattern.substring(pos);
                    throw new RuntimeException("No suffix found for placeholder pattern: " + s);
//                    expr.add(s);
//                    break;
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

        public List<ExprItem> getAllItems() {
            return Collections.unmodifiableList(itemList);
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

        public boolean isKey() {
            return isKey;
        }
    }

    public interface ParamValueFormatter {
        Object format(Map<String, Object> paramValues, String key);
    }
}
