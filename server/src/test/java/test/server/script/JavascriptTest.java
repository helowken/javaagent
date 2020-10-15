package test.server.script;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Test;

import javax.script.*;
import java.io.FileNotFoundException;
import java.net.URL;
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
//        doTest1(manager);
//        doTest2(manager);
        doTest3(manager);
    }

    private void doTest3(ScriptEngineManager manager) throws Exception {
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        eval(engine, "funcs.js");
        String script = "func1(); func2(11, \"ss\"); print(c);";
        engine.eval(script);
        System.out.println("=================");
        engine.setContext(new SimpleScriptContext());
        eval(engine, "funcs2.js");
        engine.eval("func3(); func4(2.3);");
        printBindings("ENGINE", engine.getBindings(ScriptContext.ENGINE_SCOPE));
        engine.eval(script);
    }

    private void doTest2(ScriptEngineManager manager) throws Exception {
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        ScriptEngine engine2 = manager.getEngineByName("JavaScript");

        eval(engine, "funcs.js");
        manager.getBindings().putAll(engine.getBindings(ScriptContext.ENGINE_SCOPE));
        printBindings("Global", manager.getBindings());

        String script = "func1(); func2(11, \"ss\");";
        engine2.eval(script);
        System.out.println("=================");


        engine = manager.getEngineByName("JavaScript");
//        engine.setContext(new SimpleScriptContext());
        eval(engine, "funcs2.js");

        manager.getBindings().clear();
        printBindings("Global", manager.getBindings());
        manager.getBindings().putAll(engine.getBindings(ScriptContext.ENGINE_SCOPE));
        printBindings("Global", manager.getBindings());

        String script2 = "func3(); func4(44);";
        engine2.eval(script2);
        System.out.println("=================");
        engine2.eval(script);
    }

    private void printBindings(String title, Bindings bindings) {
        System.out.println("--------------- " + title);
        bindings.forEach(
                (k, v) -> System.out.println(k + ": " + v)
        );
        System.out.println("--------------------------");
    }

    private void eval(ScriptEngine engine, String... fileNames) throws Exception {
        for (String fileName : fileNames) {
            URL url = getClass().getClassLoader().getResource(fileName);
            if (url == null)
                throw new FileNotFoundException("File not exists: " + fileName);
            engine.eval(
                    Files.newBufferedReader(
                            Paths.get(
                                    url.toURI()
                            ),
                            StandardCharsets.UTF_8
                    )
            );
        }
    }

    private void doTest1(ScriptEngineManager manager) throws Exception {
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        eval(engine, "test.js", "test2.js");
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
