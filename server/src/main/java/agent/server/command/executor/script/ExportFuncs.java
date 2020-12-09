package agent.server.command.executor.script;

import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.jvmti.JvmtiUtils;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.*;

import static agent.base.utils.AssertUtils.assertNotNull;
import static agent.server.command.executor.script.ExportUtils.listFields;
import static agent.server.command.executor.script.ExportUtils.listMethods;

public class ExportFuncs {
    public static final ExportFuncs instance = new ExportFuncs();

    private ExportFuncs() {
    }

    public Collection<String> help() {
        return listMethods(
                this,
                method -> {
                    int modifiers = method.getModifiers();
                    return Modifier.isPublic(modifiers) &&
                            !Modifier.isStatic(modifiers);
                }
        );
    }

    public Collection<String> methods(Object classOrClassName) throws Exception {
        return listMethods(
                cls(classOrClassName),
                null
        );
    }

    public Collection<String> fields(Object classOrClassName) throws Exception {
        return listFields(
                cls(classOrClassName)
        );
    }

    public Map<String, Object> clsInfo(Object classOrClassName) throws Exception {
        Class<?> clazz = cls(classOrClassName);
        Map<String, Object> rsMap = new LinkedHashMap<>();
        rsMap.put(
                "Fields",
                listFields(clazz)
        );
        rsMap.put(
                "Methods",
                listMethods(clazz, null)
        );
        return rsMap;
    }

    public Class<?> cls(Object classOrClassName) throws Exception {
        return ReflectionUtils.convert(classOrClassName);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> objs(Object classOrClassName, int maxCount) throws Exception {
        return JvmtiUtils.getInstance().findObjectsByClass(
                (Class) cls(classOrClassName),
                maxCount
        );
    }

    public <T> T obj(Object classOrClassName) throws Exception {
        List<T> rsList = objs(classOrClassName, 1);
        return rsList.isEmpty() ? null : rsList.get(0);
    }

    public Object fv(Object o, String fieldName) throws Exception {
        assertNotNull(o, "Object is null!");
        assertNotNull(fieldName, "Field name is null!");
        return ReflectionUtils.getFieldValue(fieldName, o);
    }

    public void dumpStackTrace() throws Exception {
        dumpStackTrace(null, false);
    }

    public void dumpStackTrace(String filePath, boolean append) throws Exception {
        if (filePath == null)
            IOUtils.writeToConsole(this::doDumpStackTrace);
        else
            IOUtils.write(filePath, append, this::doDumpStackTrace);
    }

    private void doDumpStackTrace(Writer writer) {
        PrintWriter pw = new PrintWriter(writer);
        try {
            Exception e = new Exception("Stack Trace");
            StackTraceElement[] els = e.fillInStackTrace().getStackTrace();
            List<StackTraceElement> elList = Arrays.asList(els);
            Collections.reverse(elList);
            LinkedList<StackTraceElement> rsList = new LinkedList<>();
            for (StackTraceElement el : elList) {
                if (isSkipStackTraceElement(el))
                    break;
                else
                    rsList.addFirst(el);
            }
            e.setStackTrace(
                    rsList.toArray(new StackTraceElement[0])
            );
            e.printStackTrace(pw);
        } finally {
            IOUtils.close(pw);
        }
    }

    private boolean isSkipStackTraceElement(StackTraceElement el) {
        String className = el.getClassName();
        return className.startsWith("agent.") ||
                (className.equals("javax.script.AbstractScriptEngine") && el.getMethodName().equals("eval"));
    }
}
