package agent.server.utils;

import java.util.stream.Stream;

public class ExportUtils {
    private static final ExportUtils instance = new ExportUtils();

    private ExportUtils() {
    }

    public void printStackTrace() {
        Thread.dumpStack();
        System.out.println("==========================");
        Thread.getAllStackTraces().forEach(
                (thread, stackFrames) -> {
                    System.out.println("---------------------" + thread.getId() + ", " + thread.getName() + "----------------------");
                    Stream.of(stackFrames).forEach(
                            sf -> System.out.println(sf.getClassName() + ": " + sf.getMethodName())
                    );
                }
        );
    }

    public static void main(String[] args) throws Exception {
//        Map<String, Object> pvs = new HashMap<>();
//        pvs.put("au", instance);
//        ScriptUtils.eval("au.printStackTrace()", pvs);
    }
}
