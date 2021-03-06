package agent.builtin.tools.execute;

import agent.base.struct.impl.Struct;
import agent.base.struct.impl.StructContext;
import agent.base.utils.Utils;
import agent.builtin.tools.config.TraceResultConfig;
import agent.builtin.tools.execute.tree.TraceRsTreeConverter;
import agent.builtin.transformer.utils.TraceItem;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;
import agent.common.tree.INode;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.DataInput;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class TraceResultCmdExecutor extends AbstractCmdExecutor {
    private static final String KEY_VALUE_CACHE = "VALUE_CACHE";
    private static final StructContext context = new StructContext();

    static {
        context.setPojoCreator(
                type -> type == TraceItem.POJO_TYPE ? new TraceItem() : null
        );
    }

    @Override
    protected ExecResult doExec(Command cmd) throws Exception {
        TraceResultConfig config = cmd.getContent();
        String inputPath = config.getInputPath();
        File dataFile = new File(inputPath);
        Map<Integer, InvokeMetadata> idToMetadata = ResultExecUtils.readInvokeMetadata(inputPath);
        int headNum = config.getHeadNum();
        int tailNum = config.getTailNum();
        if (headNum > 0 || tailNum > 0) {
            RandomAccessFile ra = new RandomAccessFile(dataFile, "r");
            final long fileSize = ra.length();
            Consumer<Integer> func = count -> Utils.wrapToRtError(
                    () -> {
                        long leftSize = fileSize;
                        for (int i = 0; i < count; ++i) {
                            leftSize -= displayTree(ra, idToMetadata, config);
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
            ResultExecUtils.calculateBinaryFile(
                    dataFile,
                    in -> displayTree(in, idToMetadata, config)
            );
        }
        return null;
    }


    private int displayTree(DataInput dataInput, Map<Integer, InvokeMetadata> idToMetadata, TraceResultConfig rsConfig) throws Exception {
        AtomicReference<Tree<TraceItem>> ref = new AtomicReference<>();
        Map<Integer, String> valueCache = new HashMap<>();
        int totalSize = ResultExecUtils.deserializeBytes(
                dataInput,
                bb -> ref.set(
                        processTree(bb, valueCache)
                ),
                true
        );
        Tree<TraceItem> tree = ref.get();
        if (tree == null)
            throw new RuntimeException("No tree found!");
        if (tree.hasChild()) {
            TreeUtils.traverse(
                    tree,
                    INode::reverseChildren
            );
            Tree<String> rsTree = new TraceRsTreeConverter(valueCache).convertTree(tree, idToMetadata, rsConfig);
            TreeUtils.printTree(
                    rsTree,
                    new TreeUtils.PrintConfig(false),
                    (node, config) -> node.getData()
            );
        }
        return totalSize;
    }

    @SuppressWarnings("unchecked")
    private Tree<TraceItem> processTree(ByteBuffer bb, Map<Integer, String> valueCache) {
        Map<Integer, Node<TraceItem>> idToNode = new HashMap<>();
        List<Object> values = Struct.deserialize(bb, context);
        valueCache.putAll(
                (Map) values.get(0)
        );
        List<TraceItem> traceItemList = (List) values.get(1);
        Tree<TraceItem> tree = new Tree<>();
        tree.setUserProp(KEY_VALUE_CACHE, valueCache);
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

}
