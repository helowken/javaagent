package agent.launcher.instrument;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InstrumentSupplyLauncher {
    private static final Queue<Instrumentation> instrumentationList = new ConcurrentLinkedQueue<>();

    public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
        instrumentationList.add(instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Exception {
        instrumentationList.add(instrumentation);
    }

    public static List<Instrumentation> getInstrumentationList() {
        return new ArrayList<>(instrumentationList);
    }
}
