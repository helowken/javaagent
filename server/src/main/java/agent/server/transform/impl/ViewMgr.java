package agent.server.transform.impl;

import agent.base.utils.InvokeDescriptorUtils;
import agent.server.transform.InvokeFinder;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.tools.asm.ProxyTransformMgr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ViewMgr {
    public static final int VIEW_CONTEXT = 0;
    public static final int VIEW_CLASS = 1;
    public static final int VIEW_INVOKE = 2;
    public static final int VIEW_PROXY = 3;

    private static Pattern newPattern(String regExp) {
        return regExp == null ? null : Pattern.compile(regExp);
    }

    private static boolean match(Pattern pattern, String value) {
        return pattern == null || pattern.matcher(value).matches();
    }

    public static Object create(int maxLevel, String contextRegexp, String classRegExp, String invokeRegExp) {
        final Pattern contextPattern = newPattern(contextRegexp);
        final Pattern classPattern = newPattern(classRegExp);
        final Pattern invokePattern = invokeRegExp == null ? null : InvokeFinder.compilePattern(invokeRegExp);
        final Collection<Pattern> invokePatterns = invokePattern == null ? null : Collections.singleton(invokePattern);
        return DestInvokeIdRegistry.getInstance().run(
                contextToClassToInvokeToId -> newValue(
                        VIEW_CONTEXT,
                        maxLevel,
                        contextToClassToInvokeToId,
                        (currLevel, mLevel, value) -> {
                            switch (currLevel) {
                                case VIEW_CONTEXT:
                                    return match(contextPattern, (String) value);
                                case VIEW_CLASS:
                                    return match(
                                            classPattern,
                                            ((Class<?>) value).getName()
                                    );
                                case VIEW_INVOKE:
                                    return invokePatterns == null || InvokeFinder.isMatch(invokePatterns, (DestInvoke) value);
                                case VIEW_PROXY:
                                default:
                                    throw new RuntimeException("Unsupport level: " + currLevel);
                            }
                        },
                        (currLevel, mLevel, value) -> {
                            switch (currLevel) {
                                case VIEW_CONTEXT:
                                    return value;
                                case VIEW_CLASS:
                                    return ((Class<?>) value).getName();
                                case VIEW_INVOKE:
                                    DestInvoke destInvoke = (DestInvoke) value;
                                    return  InvokeDescriptorUtils.descToText(destInvoke.getName() + destInvoke.getDescriptor(), true);
                                case VIEW_PROXY:
                                    Integer invokeId = (Integer) value;
                                    return ProxyTransformMgr.getInstance().getCallSiteDisplay(invokeId);
                            }
                            throw new RuntimeException("Unsupported level: " + currLevel);
                        }
                )
        );
    }

    @SuppressWarnings("unchecked")
    private static Object newValue(int currLevel, final int maxLevel, final Object source, final FilterFunc filterFunc, final DisplayFunc displayFunc) {
        if (source instanceof Map) {
            Map map = (Map) source;
            if (currLevel == maxLevel)
                return map.keySet().stream()
                        .filter(
                                key -> filterFunc.accept(currLevel, maxLevel, key)
                        )
                        .map(
                                key -> displayFunc.apply(currLevel, maxLevel, key)
                        )
                        .collect(
                                Collectors.toList()
                        );

            int nextLevel = currLevel + 1;
            if (nextLevel > maxLevel)
                throw new RuntimeException("Next level > max level");
            Map<Object, Object> rsMap = new HashMap<>();
            map.forEach(
                    (key, value) -> {
                        if (filterFunc.accept(currLevel, maxLevel, key)) {
                            Object rsValue = newValue(nextLevel, maxLevel, value, filterFunc, displayFunc);
                            if ((rsValue instanceof Map && !((Map) rsValue).isEmpty()) ||
                                    (rsValue instanceof Collection && !((Collection) rsValue).isEmpty()) ||
                                    rsValue != null)
                                rsMap.put(
                                        displayFunc.apply(currLevel, maxLevel, key),
                                        rsValue
                                );
                        }
                    }
            );
            return rsMap;
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
