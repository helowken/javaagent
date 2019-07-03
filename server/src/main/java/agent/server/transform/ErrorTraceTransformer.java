package agent.server.transform;

import java.lang.instrument.ClassFileTransformer;

public interface ErrorTraceTransformer extends ClassFileTransformer {
    Exception getError();

    boolean hasError();
}
