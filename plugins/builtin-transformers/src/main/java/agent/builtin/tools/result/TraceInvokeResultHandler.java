package agent.builtin.tools.result;

import agent.base.utils.IndentUtils;
import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.builtin.tools.result.filter.TraceResultFilter;
import agent.builtin.tools.result.filter.TreeResultConverter;
import agent.builtin.tools.result.parse.TraceResultParams;
import agent.builtin.transformer.utils.DefaultValueConverter;
import agent.builtin.transformer.utils.TraceItem;
import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructContext;
import agent.common.tree.INode;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.DataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static agent.builtin.transformer.utils.DefaultValueConverter.*;


public class TraceInvokeResultHandler extends AbstractResultHandler<Collection<Tree<TraceItem>>, TraceResultParams> {
    private static final Logger logger = Logger.getLogger(TraceInvokeResultHandler.class);
    private static final String KEY_VALUE_CACHE = "VALUE_CACHE";
    private static final String indent = IndentUtils.getIndent(1);
    private static final StructContext context = new StructContext();

    static {
        context.setPojoCreator(
                type -> type == TraceItem.POJO_TYPE ? new TraceItem() : null
        );
    }

    @Override
    public void exec(TraceResultParams params) throws Exception {
        logger.debug("Params: {}", params);
        String inputPath = params.getInputPath();
        File dataFile = new File(inputPath);
        if (!dataFile.exists())
            throw new FileNotFoundException("File not exists: " + inputPath);
        doCalculate(
                dataFile,
                readInvokeMetadata(inputPath),
                params
        );
    }

    private Tree<TraceItem> transform(Tree<TraceItem> tree) {
        TreeUtils.traverse(
                tree,
                INode::reverseChildren
        );
        return tree;
    }

    private void addWrapIfNeeded(StringBuilder sb) {
        if (sb.charAt(sb.length() - 1) != '\n')
            sb.append('\n');
    }

    private StringBuilder appendArg(StringBuilder sb, Map<String, Object> map, TraceResultParams params) {
        if (map.containsKey(KEY_INDEX)) {
            sb.append('[').append(
                    map.remove(KEY_INDEX)
            ).append("] ");
        }
        return append(sb, map, params);
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

    private void appendValue(StringBuilder sb, Map<String, Object> rsMap, TraceResultParams params) {
        if (rsMap.containsKey(KEY_VALUE)) {
            sb.append(
                    formatContent(
                            rsMap.remove(KEY_VALUE),
                            params
                    )
            );
        }
    }

    private StringBuilder append(StringBuilder sb, Map<String, Object> rsMap, TraceResultParams params) {
        appendClassName(sb, rsMap);
        sb.append(": ");
        appendValue(sb, rsMap, params);
        int i = 0;
        for (Map.Entry<String, Object> entry : rsMap.entrySet()) {
            if (i > 0)
                sb.append(", ");
            sb.append(
                    entry.getKey()
            ).append("=").append(
                    formatContent(
                            entry.getValue(),
                            params
                    )
            );
            ++i;
        }
        sb.append('\n');
        return sb;
    }

    private String formatContent(Object value, TraceResultParams params) {
        if (value == null)
            return null;
        String content = value.toString();
        int contentSize = params.getContentSize();
        return content.length() > contentSize ?
                content.substring(0, contentSize) + "... (first " + contentSize + " chars)" :
                content;
    }

    @Override
    Collection<Tree<TraceItem>> calculate(List<File> dataFiles, TraceResultParams params) {
        return null;
    }

    private void doCalculate(File dataFile, Map<Integer, InvokeMetadata> idToMetadata, TraceResultParams params) throws Exception {
        TraceResultTreeConverter converter = new TraceResultTreeConverter();
        int headNum = params.getHeadNumber();
        int tailNum = params.getTailNumber();
        if (headNum > 0 || tailNum > 0) {
            RandomAccessFile ra = new RandomAccessFile(dataFile, "r");
            final long fileSize = ra.length();
            Consumer<Integer> func = count -> Utils.wrapToRtError(
                    () -> {
                        long leftSize = fileSize;
                        for (int i = 0; i < count; ++i) {
                            leftSize -= displayTree(ra, idToMetadata, params, converter);
                            if (leftSize <= 0)
                                break;
                        }
                    }
            );

            if (headNum > 0)
                func.accept(headNum);

            if (tailNum > 0) {
                long pos = fileSize;
                int sizeLen = Integer.BYTES;
                int dataLen;
                ra.seek(pos - sizeLen);
                for (int i = 0; i < tailNum; ++i) {
                    dataLen = ra.readInt();
                    // format is: [size] [data] [size] | [size[ [data] [size]
                    // so need to skip sizeLen * 3 to get the previous size at tail.
                    pos -= (dataLen + sizeLen * 2);
                    if (pos > 0) {
                        if (i < tailNum - 1)
                            ra.seek(pos - sizeLen);  // there are more than one records
                        else
                            ra.seek(pos);
                    } else {
                        ra.seek(pos);
                        break;
                    }
                }
                func.accept(tailNum);
            }
        } else {
            calculateBinaryFile(
                    dataFile,
                    in -> displayTree(in, idToMetadata, params, converter)
            );
        }
    }

    private int displayTree(DataInput dataInput, Map<Integer, InvokeMetadata> idToMetadata, TraceResultParams params,
                            TraceResultTreeConverter converter) throws Exception {
        AtomicReference<Tree<TraceItem>> ref = new AtomicReference<>();
        int totalSize = deserializeBytes(
                dataInput,
                bb -> ref.set(
                        processTree(bb)
                ),
                true
        );
        Tree<TraceItem> tree = ref.get();
        if (tree == null)
            throw new RuntimeException("No tree found!");
        if (tree.hasChild()) {
            Tree<String> rsTree = converter.convert(
                    transform(tree),
                    idToMetadata,
                    params
            );
            TreeUtils.printTree(
                    rsTree,
                    new TreeUtils.PrintConfig(false),
                    (node, config) -> node.getData()
            );
        }
        return totalSize;
    }

    @SuppressWarnings("unchecked")
    private Tree<TraceItem> processTree(ByteBuffer bb) {
        Map<Integer, Node<TraceItem>> idToNode = new HashMap<>();
        List<Object> values = Struct.deserialize(bb, context);
        Map<Integer, String> idxToValue = (Map) values.get(0);
        List<TraceItem> traceItemList = (List) values.get(1);
        Tree<TraceItem> tree = new Tree<>();
        tree.setUserProp(KEY_VALUE_CACHE, idxToValue);
        idToNode.put(-1, tree);
        traceItemList.forEach(
                item -> {
                    int id = item.getId();
                    if (idToNode.containsKey(id))
                        throw new RuntimeException("Duplicated node id: " + id);
                    idToNode.put(
                            id,
                            new Node<>(item)
                    );
                }
        );
        traceItemList.forEach(
                item -> {
                    Node<TraceItem> pn = idToNode.get(
                            item.getParentId()
                    );
                    if (pn == null)
                        throw new RuntimeException("No parent node found by: " + item.getParentId());
                    Node<TraceItem> node = idToNode.get(
                            item.getId()
                    );
                    if (node == null)
                        throw new RuntimeException("No node found by : " + item.getId());
                    pn.appendChild(node);
                }
        );
        return tree;
    }

    private class TraceResultTreeConverter extends TreeResultConverter<TraceItem, TraceResultParams, String> {
        @Override
        protected TraceResultFilter createFilter() {
            return new TraceResultFilter();
        }

        @Override
        protected InvokeMetadata findMetadata(Map<Integer, InvokeMetadata> idToMetadata, TraceItem data) {
            return getMetadata(
                    idToMetadata,
                    data.getInvokeId()
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Node<String> createNode(Node<TraceItem> node, Map<Integer, InvokeMetadata> idToMetadata,
                                          InvokeMetadata metadata, TraceResultParams params) {
            Map<Integer, String> valueCache = (Map) node.getRoot().getUserProp(KEY_VALUE_CACHE);
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

            return item.getType() == TraceItem.TYPE_INVOKE ?
                    createInvokeNode(node, idToMetadata, metadata, params) :
                    createCatchNode(node, params);
        }

        private Node<String> createCatchNode(Node<TraceItem> node, TraceResultParams params) {
            StringBuilder sb = new StringBuilder();
            TraceItem item = node.getData();
            if (params.isDisplayError()) {
                appendError("Catch Error", sb, item, params);
            } else {
                sb.append("Catch Error: ");
                appendClassName(
                        sb,
                        new TreeMap<>(
                                item.getError()
                        )
                );
            }
            return new Node<>(
                    sb.toString()
            );
        }

        private Node<String> createInvokeNode(Node<TraceItem> node, Map<Integer, InvokeMetadata> idToMetadata,
                                              InvokeMetadata metadata, TraceResultParams params) {
            StringBuilder sb = new StringBuilder();
            TraceItem item = node.getData();
            if (params.isDisplayTime())
                sb.append("[").append(
                        item.costTimeString()
                ).append("ms] ");

            sb.append(
                    convertInvoke(
                            item.getParentId() == -1 ? null : node.getParent().getData().getInvokeId(),
                            idToMetadata,
                            metadata
                    )
            );
            if (item.hasArgs() && params.isDisplayArgs()) {
                sb.append("\nArgs: \n");
                item.getArgs().forEach(
                        arg -> appendArg(
                                sb.append(indent),
                                new TreeMap<>(arg),
                                params
                        )
                );
            }
            if (item.hasReturnValue() && params.isDisplayRetValue()) {
                String className = (String) item.getReturnValue().get(KEY_CLASS);
                if (className == null || !className.equals(void.class.getName())) {
                    addWrapIfNeeded(sb);
                    append(
                            sb.append("Return: \n").append(indent),
                            new TreeMap<>(
                                    item.getReturnValue()
                            ),
                            params
                    );
                }
            }
            if (item.hasError() && params.isDisplayError()) {
                addWrapIfNeeded(sb);
                appendError("Throw Error", sb, item, params);
            }
            return new Node<>(
                    sb.toString()
            );
        }

        private void appendError(String prefix, StringBuilder sb, TraceItem item, TraceResultParams params) {
            append(
                    sb.append(prefix).append(": \n").append(indent),
                    new TreeMap<>(
                            item.getError()
                    ),
                    params
            );
        }
    }
}
