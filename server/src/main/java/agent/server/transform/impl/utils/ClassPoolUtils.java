package agent.server.transform.impl.utils;

import agent.base.utils.Utils;

import java.util.Optional;

public class ClassPoolUtils {
    private static final ThreadLocal<ClassPathRecorder> classPathRecorderLocal = new ThreadLocal<>();

    public static void exec(ClassPoolFunc func) {
        AgentClassPool cp = AgentClassPool.getInstance();
        ClassPathRecorder classPathRecorder = new ClassPathRecorder(cp);
        classPathRecorderLocal.set(classPathRecorder);
        Utils.wrapToRtError(() -> {
            try {
                func.exec(cp, classPathRecorder);
            } finally {
                classPathRecorder.clear();
                cp.clear();
                classPathRecorderLocal.remove();
            }
        });
    }

    public static ClassPathRecorder getClassPathRecorder() {
        return Optional.ofNullable(
                classPathRecorderLocal.get()
        ).orElseThrow(
                () -> new RuntimeException("No class path recorder found.")
        );
    }

    public interface ClassPoolFunc {
        void exec(AgentClassPool cp, ClassPathRecorder classPathRecorder) throws Exception;
    }
}
