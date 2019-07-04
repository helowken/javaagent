package agent.server.transform;

import java.lang.instrument.ClassFileTransformer;
import java.util.Set;

public interface ErrorTraceTransformer extends ClassFileTransformer {
    Exception getError();

    boolean hasError();

    Set<Class<?>> getRefClassSet();
}
