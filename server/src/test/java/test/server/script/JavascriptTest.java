package test.server.script;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JavascriptTest {
    @Test
    public void test() throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        engine.eval(
                Files.newBufferedReader(Paths.get("/home/helowken/projects/javaagent/server/src/test/resources/test.js"), StandardCharsets.UTF_8)
        );
        engine.eval(
                Files.newBufferedReader(Paths.get("/home/helowken/projects/javaagent/server/src/test/resources/test2.js"), StandardCharsets.UTF_8)
        );
        engine.put("aa", new AAA());
        manager.getBindings().put("bb", new Date());

        Map<String, Object> pvs = new HashMap<>();
        pvs.put("methodName", "xxxxxx");
        final Invocable inv = (Invocable) engine;
        String[] methodNames = new String[]{
//                "onBefore_test1",
//                "onAfter_test1",
//                "onReturning_test1",
//                "onThrowing_test1",
//                "onCatching_test1",
                "dump"
        };
        for (String methodName : methodNames) {
            final String mn = methodName;
            inv.invokeFunction(mn, pvs);
        }
        Object o = engine.get("onBefore_test1");
        if (o instanceof ScriptObjectMirror) {
            System.out.println(
                    ((ScriptObjectMirror) o).isFunction()
            );
        }
        System.out.println(engine.get("aa"));
        System.out.println(engine.get("bb"));
    }

    public static class AAA {
        public void test() {
            System.out.println("Test in AAA.");
            Thread.dumpStack();
        }
    }
}
