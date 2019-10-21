package agent.server.transform;

import java.lang.instrument.ClassFileTransformer;
import java.util.Set;

public interface ErrorTraceTransformer extends ClassFileTransformer {
    Throwable getError();

    boolean hasError();

    Set<Class<?>> getRefClassSet();
}
