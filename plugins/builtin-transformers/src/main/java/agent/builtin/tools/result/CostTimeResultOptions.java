package agent.builtin.tools.result;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class CostTimeResultOptions extends ResultOptions {
    static final Set<Float> DEFAULT_RATES = Collections.unmodifiableSet(
            new TreeSet<>(
                    Arrays.asList(0.9F, 0.95F, 0.99F)
            )
    );
    public Set<Float> rates = DEFAULT_RATES;
}
