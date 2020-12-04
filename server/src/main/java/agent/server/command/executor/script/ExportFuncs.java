package agent.server.command.executor.script;

import agent.base.utils.ReflectionUtils;
import agent.jvmti.JvmtiUtils;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static agent.base.utils.AssertUtils.assertNotNull;
import static agent.server.command.executor.script.ExportUtils.*;

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
        checkClassOrClassName(classOrClassName);
        return classOrClassName instanceof String ?
                ReflectionUtils.findClass((String) classOrClassName) :
                (Class) classOrClassName;
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
}
