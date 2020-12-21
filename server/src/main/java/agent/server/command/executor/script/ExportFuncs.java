package agent.server.command.executor.script;

import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.common.args.parse.FilterOptUtils;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.TransformerData;
import agent.server.transform.TransformerRegistry;
import agent.server.transform.search.ClassCache;
import agent.server.transform.search.ClassSearcher;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.*;

import static agent.server.command.executor.script.ExportUtils.listFields;
import static agent.server.command.executor.script.ExportUtils.listMethods;

@SuppressWarnings("unchecked")
public class ExportFuncs {
    private static final int DEFAULT_MAX_COUNT = 100;
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
        if (classOrClassName instanceof Class)
            return (Class<?>) classOrClassName;
        if (classOrClassName instanceof String) {
            List<Class<?>> rsList = clses((String) classOrClassName);
            if (rsList.isEmpty())
                throw new Exception("No class found by: " + classOrClassName);
            return rsList.get(0);
        }
        throw new IllegalArgumentException("Must be class or className.");
    }

    public List<Class<?>> clses(String classStr) throws Exception {
        return new ArrayList<>(
                ClassSearcher.getInstance().search(
                        new ClassCache(),
                        FilterOptUtils.newClassFilterConfig(classStr)
                )
        );
    }

    public Collection<Class<?>> subClses(String classOrClassName) throws Exception {
        return new ClassCache().getSubClasses(
                cls(classOrClassName),
                null
        );
    }

    public Collection<Class<?>> subTypes(String classOrClassName) throws Exception {
        return new ClassCache().getSubTypes(
                cls(classOrClassName),
                null
        );
    }

    public <T> List<T> objs(Object classOrClassName) throws Exception {
        return objs(classOrClassName, DEFAULT_MAX_COUNT);
    }

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
        return ReflectionUtils.getFieldValue(fieldName, o);
    }

    public void sfv(Object o, String fieldName, Object value) throws Exception {
        ReflectionUtils.setFieldValue(fieldName, o, value);
    }

    public Object cv(Object classOrClassName, String fieldName) throws Exception {
        Class<?> clazz = cls(classOrClassName);
        return ReflectionUtils.getFieldValue(clazz, fieldName, null);
    }

    public void scv(Object classOrClassName, String fieldName, Object value) throws Exception {
        Class<?> clazz = cls(classOrClassName);
        ReflectionUtils.setFieldValue(clazz, fieldName, null, value);
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

    public TransformerData data(String tid) {
        return TransformerRegistry.getTransformerData(tid);
    }
}
