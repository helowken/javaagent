package jython;

import agent.base.utils.Logger;
import org.python.core.PyInteger;
import org.python.util.PythonInterpreter;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.StringWriter;
import java.util.List;

public class AATest {
    private static final Logger logger = Logger.getLogger(AATest.class);

    public static void main(String[] args) {
        PythonInterpreter pi = new PythonInterpreter();
        pi.set("integer", new PyInteger(42));
        pi.exec("square = integer*integer");
        PyInteger square = (PyInteger) pi.get("square");
        System.out.println("square: " + square.asInt());
//        listEngines();
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.setOut(System.out);

            pyInterp.exec("print('Hello Baeldung Readers!!')");

            String path = "/home/helowken/projects/javaagent/dependents/jython-dependent/src/main/java/jython/hello.py";
            pyInterp.execfile(path);
        }
    }

    public static void listEngines() {
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> engines = manager.getEngineFactories();

        for (ScriptEngineFactory engine : engines) {
            logger.info("Engine name: {}", engine.getEngineName());
            logger.info("Version: {}", engine.getEngineVersion());
            logger.info("Language: {}", engine.getLanguageName());

            logger.info("Short Names:");
            for (String names : engine.getNames()) {
                logger.info(names);
            }
        }
    }
}
