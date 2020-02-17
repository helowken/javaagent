package agent.server.transform.cache;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class AbstractPatternClassFilter implements ClassFilter {
    final Collection<Pattern> patterns;

    AbstractPatternClassFilter(Collection<String> regexps) {
        this.patterns = regexps.stream()
                .map(Pattern::compile)
                .collect(Collectors.toList());
    }

}
