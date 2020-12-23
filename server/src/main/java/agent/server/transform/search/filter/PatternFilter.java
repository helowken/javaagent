package agent.server.transform.search.filter;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class PatternFilter implements AgentFilter<String> {
    final Collection<Pattern> patterns;

    private PatternFilter(Collection<String> strings) {
        this.patterns = strings.stream()
                .map(Pattern::compile)
                .collect(
                        Collectors.toList()
                );
    }

    @Override
    public String toString() {
        return "PatternFilter{" +
                "patterns=" + patterns +
                '}';
    }

    public static PatternFilter include(Collection<String> strings) {
        return new Include(strings);
    }

    public static PatternFilter exclude(Collection<String> strings) {
        return new Exclude(strings);
    }

    private static class Include extends PatternFilter {
        Include(Collection<String> strings) {
            super(strings);
        }

        @Override
        public boolean accept(String v) {
            return patterns.stream()
                    .anyMatch(
                            pattern -> pattern.matcher(v).matches()
                    );
        }
    }

    private static class Exclude extends PatternFilter {
        Exclude(Collection<String> strings) {
            super(strings);
        }

        @Override
        public boolean accept(String v) {
            return patterns.stream()
                    .noneMatch(
                            pattern -> pattern.matcher(v).matches()
                    );
        }
    }
}
