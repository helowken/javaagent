package agent.server.command.executor;

import agent.base.utils.MethodDescriptorUtils;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.server.transform.ContextClassLoaderMgr;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static agent.common.message.MessageType.CMD_VIEW;
import static agent.common.message.command.impl.ViewCommand.*;

@SuppressWarnings("unchecked")
class ViewCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        String catalog = ((ViewCommand) cmd).getCatalog();
        Object value;
        switch (catalog) {
            case CATALOG_CLASS:
                value = getContextToClassSet();
                break;
            case CATALOG_INVOKE:
                value = getContextToClassToInvokes();
                break;
            case CATALOG_CLASSPATH:
                value = getContextToClasspathSet();
                break;
            default:
                throw new RuntimeException("Unknown catalog: " + catalog);
        }
        if (value == null)
            throw new RuntimeException("No result found.");
        return DefaultExecResult.toSuccess(CMD_VIEW, null, value);
    }

    private Map getContextToClassToInvokes() {
        return formatResult(
                DestInvokeIdRegistry.getInstance().getDestInvokesOfClass(null, null),
                value -> {
                    if (value instanceof Class)
                        return ((Class<?>) value).getName();
                    else if (value instanceof DestInvoke) {
                        DestInvoke invoke = (DestInvoke) value;
                        return MethodDescriptorUtils.descToText(
                                invoke.getName() + invoke.getDescriptor(),
                                true
                        );
                    }
                    return Objects.toString(value);
                }
        );
    }

    private Map getContextToClassSet() {
        return formatResult(
                DestInvokeIdRegistry.getInstance().getClassesOfContext(null),
                value -> {
                    if (value instanceof Class)
                        return ((Class<?>) value).getName();
                    return String.valueOf(value);
                }
        );
    }

    private Map getContextToClasspathSet() {
        return formatResult(
                ContextClassLoaderMgr.getInstance().getContextToClasspathSet(),
                Objects::toString
        );
    }

    private Map formatResult(Map map, Function<Object, Object> elementToStringFunc) {
        Map rsMap = new HashMap<>();
        map.forEach(
                (key, value) -> {
                    Object result;
                    if (value instanceof Map)
                        result = formatResult(
                                (Map) value,
                                elementToStringFunc
                        );
                    else if (value instanceof Collection) {
                        result = ((Collection) value).stream()
                                .map(elementToStringFunc)
                                .collect(
                                        Collectors.toList()
                                );
                    } else
                        result = elementToStringFunc.apply(value);
                    rsMap.put(
                            String.valueOf(
                                    elementToStringFunc.apply(key)
                            ),
                            result
                    );
                }
        );
        return rsMap;
    }

}
