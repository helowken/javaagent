package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.server.transform.ContextClassLoaderMgr;
import agent.server.transform.ResetClassMgr;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static agent.common.message.MessageType.CMD_VIEW;
import static agent.common.message.command.impl.ViewCommand.CATALOG_CLASS;
import static agent.common.message.command.impl.ViewCommand.CATALOG_CLASSPATH;

class ViewCmdExecutor extends AbstractCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) throws Exception {
        String catalog = ((ViewCommand) cmd).getCatalog();
        Object value;
        switch (catalog) {
            case CATALOG_CLASS:
                value = getContextToClassSet();
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

    private Map<String, Set<String>> getContextToClassSet() {
        return formatInfo(() -> ResetClassMgr.getInstance().getContextToTransformedClassSet(), Class::getName);
    }

    private Map<String, Set<String>> getContextToClasspathSet() {
        return formatInfo(() -> ContextClassLoaderMgr.getInstance().getContextToClasspathSet(), null);
    }

    private <V> Map<String, Set<String>> formatInfo(ContextInfoSupplier<V> supplier, Function<V, String> elementToStringFunc) {
        Map<String, Set<String>> rsMap = new HashMap<>();
        supplier.get().forEach((context, elements) ->
                rsMap.put(context,
                        elements.stream()
                                .map(elementToStringFunc == null ? Object::toString : elementToStringFunc)
                                .collect(Collectors.toSet())
                )
        );
        return rsMap;
    }

    private interface ContextInfoSupplier<V> {
        Map<String, ? extends Collection<V>> get();
    }

    public static void main(String[] args) {
        DefaultExecResult result = DefaultExecResult.toSuccess(CMD_VIEW, null, Collections.singletonMap("xxx", Collections.emptyList()));
        ByteBuffer bb = ByteBuffer.allocate(1024);
        result.serialize(bb);
        DefaultExecResult result2 = new DefaultExecResult();
        result2.deserialize(bb);
        System.out.println((Map) result2.getContent());
    }
}
