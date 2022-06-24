package agent.builtin.tools.execute.tree;

import agent.base.utils.IndentUtils;
import agent.base.utils.InvokeDescriptorUtils;
import agent.builtin.tools.config.TraceResultConfig;
import agent.builtin.tools.execute.ResultExecUtils;
import agent.builtin.tools.result.filter.ResultFilter;
import agent.builtin.transformer.utils.DefaultValueConverter;
import agent.builtin.transformer.utils.TraceItem;
import agent.common.tree.Node;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.util.Map;
import java.util.TreeMap;

import static agent.builtin.transformer.utils.DefaultValueConverter.*;

public class TraceRsTreeConverter extends RsTreeConverter<String, TraceItem, TraceResultConfig> {
    private static final String indent = IndentUtils.getIndent(1);
    private final Map<Integer, String> valueCache;

    public TraceRsTreeConverter(Map<Integer, String> valueCache) {
        this.valueCache = valueCache;
    }

    @Override
    protected InvokeMetadata findMetadata(Map<Integer, InvokeMetadata> idToMetadata, TraceItem data) {
        return ResultExecUtils.getMetadata(
                idToMetadata,
                data.getInvokeId()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Node<String> createNode(Node<TraceItem> node, Map<Integer, InvokeMetadata> idToMetadata, InvokeMetadata pnMetadata, TraceResultConfig config) {
        TraceItem item = node.getData();
        DefaultValueConverter.transformValues(
                item.getArgs(),
                valueCache
        );
        DefaultValueConverter.transformValue(
                item.getReturnValue(),
                valueCache
        );
        DefaultValueConverter.transformValue(
                item.getError(),
                valueCache
        );

        switch (item.getType()) {
            case TraceItem.TYPE_INVOKE:
                return createInvokeNode(node, idToMetadata, pnMetadata, config);
            case TraceItem.TYPE_THROW:
                return createThrowNode(node, config);
            case TraceItem.TYPE_CATCH:
                return createCatchNode(node, config);
            default:
                throw new RuntimeException("Unknown type: " + item.getType());
        }
    }

    @Override
    protected ResultFilter<TraceItem> getFilter(TraceResultConfig config) {
        return config.getFilter();
    }

    private Node<String> createInvokeNode(Node<TraceItem> node, Map<Integer, InvokeMetadata> idToMetadata,
                                          InvokeMetadata metadata, TraceResultConfig config) {
        StringBuilder sb = new StringBuilder();
        TraceItem item = node.getData();
        if (config.isDisplayConsumedTime())
            sb.append("[").append(
                    item.consumedTimeString()
            ).append("ms] ");

        sb.append(
                ResultExecUtils.convertInvoke(
                        item.getParentId() == -1 ? null : node.getParent().getData().getInvokeId(),
                        idToMetadata,
                        metadata,
                        config
                )
        );
        if (item.hasArgs() && config.isDisplayArgs()) {
            sb.append("\nArgs: \n");
            item.getArgs().forEach(
                    arg -> appendArg(
                            sb.append(indent),
                            new TreeMap<>(arg),
                            config
                    )
            );
        }
        if (item.hasReturnValue() && config.isDisplayRetValue()) {
            String className = (String) item.getReturnValue().get(KEY_CLASS);
            if (className == null || !className.equals(void.class.getName())) {
                addWrapIfNeeded(sb);
                append(
                        sb.append("Return: \n").append(indent),
                        new TreeMap<>(
                                item.getReturnValue()
                        ),
                        config
                );
            }
        }
        if (item.hasError()) {
            if (config.isDisplayError()) {
                addWrapIfNeeded(sb);
                appendDetailError("Raise Error:", sb, item, config);
            } else
                sb.insert(0, "[Error] ");
        }
        return new Node<>(
                sb.toString()
        );
    }

    private void appendDetailError(String prefix, StringBuilder sb, TraceItem item, TraceResultConfig config) {
        append(
                sb.append(prefix).append("\n").append(indent),
                new TreeMap<>(
                        item.getError()
                ),
                config,
                false
        );
    }

    private void appendSimpleError(String prefix, StringBuilder sb, TraceItem item) {
        sb.append(prefix);
        Map<String, Object> errMap = item.getError();
        String valueStr = (String) errMap.get(KEY_VALUE);
        int pos = valueStr.indexOf('\n');
        if (pos > -1)
            valueStr = valueStr.substring(0, pos);
        pos = valueStr.indexOf(':');
        valueStr = pos > -1 ? valueStr.substring(pos + 1) : "";
        appendClassName(
                sb,
                new TreeMap<>(errMap)
        );
        sb.append(valueStr);
    }

    private Node<String> createThrowNode(Node<TraceItem> node, TraceResultConfig config) {
        return createErrorNode("Throw Error: ", node, config);
    }

    private Node<String> createCatchNode(Node<TraceItem> node, TraceResultConfig config) {
        return createErrorNode("Catch Error: ", node, config);
    }

    private Node<String> createErrorNode(String prefix, Node<TraceItem> node, TraceResultConfig config) {
        StringBuilder sb = new StringBuilder();
        TraceItem item = node.getData();
        if (config.isDisplayError())
            appendDetailError(prefix, sb, item, config);
        else
            appendSimpleError(prefix, sb, item);
        return new Node<>(
                sb.toString()
        );
    }

    private void addWrapIfNeeded(StringBuilder sb) {
        if (sb.charAt(sb.length() - 1) != '\n')
            sb.append('\n');
    }

    private StringBuilder appendArg(StringBuilder sb, Map<String, Object> map, TraceResultConfig config) {
        if (map.containsKey(KEY_INDEX)) {
            sb.append('[').append(
                    map.remove(KEY_INDEX)
            ).append("] ");
        }
        return append(sb, map, config);
    }

    private String formatClass(String className) {
        return InvokeDescriptorUtils.shortForPkgLang(className);
    }

    private void appendClassName(StringBuilder sb, Map<String, Object> rsMap) {
        if (rsMap.containsKey(KEY_CLASS)) {
            sb.append('<').append(
                    formatClass(
                            String.valueOf(
                                    rsMap.remove(KEY_CLASS)
                            )
                    )
            ).append(">");
        }
    }

    private void appendValue(StringBuilder sb, Map<String, Object> rsMap, TraceResultConfig config) {
        if (rsMap.containsKey(KEY_VALUE)) {
            sb.append(
                    formatContent(
                            rsMap.remove(KEY_VALUE),
                            config
                    )
            );
        }
    }

    private StringBuilder append(StringBuilder sb, Map<String, Object> rsMap, TraceResultConfig config) {
        return append(sb, rsMap, config, true);
    }

    private StringBuilder append(StringBuilder sb, Map<String, Object> rsMap, TraceResultConfig config, boolean withClassName) {
        if (withClassName) {
            appendClassName(sb, rsMap);
            sb.append(": ");
        } else
            rsMap.remove(KEY_CLASS);

        appendValue(sb, rsMap, config);
        int i = 0;
        for (Map.Entry<String, Object> entry : rsMap.entrySet()) {
            if (i > 0)
                sb.append(", ");
            sb.append(
                    entry.getKey()
            ).append("=").append(
                    formatContent(
                            entry.getValue(),
                            config
                    )
            );
            ++i;
        }
        sb.append('\n');
        return sb;
    }

    private String formatContent(Object value, TraceResultConfig config) {
        if (value == null)
            return null;
        String content = value.toString();
        int contentSize = config.getContentSize();
        return content.length() > contentSize ?
                content.substring(0, contentSize) + "... (first " + contentSize + " chars)" :
                content;
    }
}
