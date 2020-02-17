package agent.server.transform.cache;

import java.util.Collection;

public class IncludeClassFilter extends AbstractPatternClassFilter {
    public IncludeClassFilter(Collection<String> regexps) {
        super(regexps);
    }

    @Override
    public boolean accept(Class<?> clazz) {
        String className = clazz.getName();
        return patterns.stream()
                .anyMatch(
                        pattern -> pattern.matcher(className).matches()
                );
    }
}
