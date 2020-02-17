package agent.server.transform.cache;

import java.util.Collection;

public class ExcludeClassFilter extends AbstractPatternClassFilter {
    public ExcludeClassFilter(Collection<String> regexps) {
        super(regexps);
    }

    @Override
    public boolean accept(Class<?> clazz) {
        String className = clazz.getName();
        return patterns.stream()
                .noneMatch(
                        pattern -> pattern.matcher(className).matches()
                );
    }
}
