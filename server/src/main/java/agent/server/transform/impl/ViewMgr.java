package agent.server.transform.impl;

import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.Utils;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.ClassFilter;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.tools.asm.ProxyTransformMgr;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ViewMgr {
    public static final int VIEW_CLASS = 0;
    public static final int VIEW_INVOKE = 1;
    public static final int VIEW_PROXY = 2;

    private static Pattern newPattern(String regExp) {
        return regExp == null ?
                null :
                Pattern.compile(
                        FilterUtils.parse(regExp)
                );
    }

    private static boolean match(Pattern pattern, String value) {
        return pattern == null || pattern.matcher(value).matches();
    }

    public static Object create(int maxLevel, String classRegExp, String invokeRegExp, String proxyRegExp) {
        final ClassFilter classFilter = FilterUtils.newClassFilter(
                classRegExp == null ? null : Collections.singleton(classRegExp),
                null,
                true
        );
        final AgentFilter<DestInvoke> invokeFilter = FilterUtils.newInvokeFilter(
                invokeRegExp == null ? null : Collections.singleton(invokeRegExp),
                null
        );
        final Pattern proxyPattern = newPattern(proxyRegExp);
        return DestInvokeIdRegistry.getInstance().run(
                classToInvokeToId -> newValue(
                        VIEW_CLASS,
                        maxLevel,
                        classToInvokeToId,
                        (currLevel, mLevel, value) -> {
                            switch (currLevel) {
                                case VIEW_CLASS:
                                    return FilterUtils.isAccept(
                                            classFilter,
                                            (Class<?>) value
                                    );
                                case VIEW_INVOKE:
                                    return FilterUtils.isAccept(
                                            invokeFilter,
                                            (DestInvoke) value
                                    );
                                case VIEW_PROXY:
                                default:
                                    throw new RuntimeException("Unsupport level: " + currLevel);
                            }
                        },
                        (currLevel, mLevel, value) -> {
                            switch (currLevel) {
                                case VIEW_CLASS:
                                    return ((Class<?>) value).getName();
                                case VIEW_INVOKE:
                                    DestInvoke destInvoke = (DestInvoke) value;
                                    return InvokeDescriptorUtils.descToText(
                                            destInvoke.getFullName()
                                    );
                                case VIEW_PROXY:
                                    return filterProxy(
                                            (Integer) value,
                                            proxyPattern
                                    );
                            }
                            throw new RuntimeException("Unsupported level: " + currLevel);
                        }
                )
        );
    }

    private static Map<String, List<String>> filterProxy(Integer invokeId, Pattern proxyPattern) {
        Map<String, List<String>> proxyMap = ProxyTransformMgr.getInstance().getCallSiteDisplay(invokeId);
        Map<String, List<String>> rsMap = new TreeMap<>();
        proxyMap.forEach(
                (pos, proxyList) -> {
                    List<String> rsList = proxyList.stream()
                            .filter(
                                    proxy -> match(proxyPattern, proxy)
                            )
                            .collect(
                                    Collectors.toList()
                            );
                    if (!rsList.isEmpty())
                        rsMap.put(pos, rsList);
                }
        );
        return Utils.convertEmptyToNull(rsMap);
    }

    @SuppressWarnings("unchecked")
    private static Object newValue(int currLevel, final int maxLevel, final Object source, final FilterFunc filterFunc, final DisplayFunc displayFunc) {
        if (source instanceof Map) {
            Map<?, ?> map = (Map) source;
            if (currLevel == maxLevel)
                return Utils.convertEmptyToNull(
                        map.keySet().stream()
                                .filter(
                                        key -> filterFunc.accept(currLevel, maxLevel, key)
                                )
                                .map(
                                        key -> displayFunc.apply(currLevel, maxLevel, key)
                                )
                                .filter(Objects::nonNull)
                                .sorted()
                                .collect(
                                        Collectors.toList()
                                )
                );

            int nextLevel = currLevel + 1;
            if (nextLevel > maxLevel)
                throw new RuntimeException("Next level > max level");
            Map<Object, Object> rsMap = new TreeMap<>();
            map.forEach(
                    (key, value) -> {
                        if (filterFunc.accept(currLevel, maxLevel, key)) {
                            Object rsValue = newValue(nextLevel, maxLevel, value, filterFunc, displayFunc);
                            if (rsValue != null)
                                rsMap.put(
                                        displayFunc.apply(currLevel, maxLevel, key),
                                        rsValue
                                );
                        }
                    }
            );
            return Utils.convertEmptyToNull(rsMap);
        }
        return displayFunc.apply(currLevel, maxLevel, source);
    }

    private interface FilterFunc {
        boolean accept(int currLevel, int maxLevel, Object value);
    }

    private interface DisplayFunc {
        Object apply(int currLevel, int maxLevel, Object value);
    }
}
