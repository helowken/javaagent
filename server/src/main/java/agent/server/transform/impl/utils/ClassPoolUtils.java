package agent.server.transform.impl.utils;

public class ClassPoolUtils {

    public static void exec(ClassPoolFunc func) {
        AgentClassPool cp = AgentClassPool.getInstance();
        ClassPathRecorder classPathRecorder = new ClassPathRecorder(cp);
        try {
            func.exec(cp, classPathRecorder);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            classPathRecorder.clear();
            cp.clear();
        }
    }


    public interface ClassPoolFunc {
        void exec(AgentClassPool cp, ClassPathRecorder classPathRecorder) throws Exception;
    }
}
