package agent.builtin.tools.execute;

import agent.base.struct.impl.Struct;
import agent.base.struct.impl.StructContext;
import agent.base.utils.IOUtils;
import agent.base.utils.Pair;
import agent.builtin.tools.config.TraceResultConfig;
import agent.builtin.tools.execute.tree.TraceRsTreeConverter;
import agent.builtin.transformer.utils.TraceItem;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.DataInput;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TraceResultCmdExecutor extends AbstractCmdExecutor {
    private static final String KEY_VALUE_CACHE = "VALUE_CACHE";
    private static final StructContext context = new StructContext();

    static {
        context.setPojoCreator(
                type -> type == TraceItem.POJO_TYPE ? new TraceItem() : null
        );
    }

    private void readFromHead(RandomAccessFile raFile, Map<Integer, InvokeMetadata> idToMetadata,
                              TraceResultConfig config, int headNum, final long fileSize, List<Tree<String>> rsList) throws Exception {
        Pair<Integer, Tree<String>> rs;
        int i = 0;
        long leftSize = fileSize;
        while (i < headNum && leftSize > 0) {
            rs = computeTree(raFile, idToMetadata, config);
            if (rs.right != null) {
                rsList.add(rs.right);
                ++i;
            }
            leftSize -= rs.left;
        }
    }

    private void readFromTail(RandomAccessFile raFile, Map<Integer, InvokeMetadata> idToMetadata,
                              TraceResultConfig config, int tailNum, final long fileSize, List<Tree<String>> rsList) throws Exception {
        Pair<Integer, Tree<String>> rs;
        long pos = fileSize;
        int sizeLen = Integer.BYTES;
        int dataLen;
        int i = 0;
        // format is: ... | [size] [data] [size] | [size[ [data] [size] | ...
        while (i < tailNum && pos >= sizeLen) {
            pos -= sizeLen;     /* ^ indicates the curr pos: ... | [size] [data] ^ [size] */
            raFile.seek(pos);
            dataLen = raFile.readInt();

            pos -= (dataLen + sizeLen);     /* ... | ^ [size] [data] [size] */
            if (pos < 0)
                break;  /* Maybe a bug. */
            raFile.seek(pos);

            rs = computeTree(raFile, idToMetadata, config);
            if (rs.right != null) {
                rsList.add(rs.right);
                ++i;
            }
        }
    }

    @Override
    protected ExecResult doExec(Command cmd) throws Exception {
        TraceResultConfig config = cmd.getContent();
        String inputPath = config.getInputPath();
        File dataFile = new File(inputPath);
        Map<Integer, InvokeMetadata> idToMetadata = ResultExecUtils.readInvokeMetadata(inputPath);
        int headNum = config.getHeadNum();
        int tailNum = config.getTailNum();
        List<Tree<String>> rsList = new ArrayList<>(10);
        if (headNum > 0 || tailNum > 0) {
            RandomAccessFile raFile = new RandomAccessFile(dataFile, "r");
            try {
                final long fileSize = raFile.length();
                if (headNum > 0)
                    readFromHead(raFile, idToMetadata, config, headNum, fileSize, rsList);
                if (tailNum > 0)
                    readFromTail(raFile, idToMetadata, config, tailNum, fileSize, rsList);
            } finally {
                IOUtils.close(raFile);
            }
        } else {
            ResultExecUtils.calculateBinaryFile(
                    dataFile,
                    in -> {
                        Pair<Integer, Tree<String>> rs = computeTree(in, idToMetadata, config);
                        if (rs.right != null)
                            rsList.add(rs.right);
                        return rs.left;
                    }
            );
        }
        rsList.forEach(this::printTree);
        return null;
    }


    private Pair<Integer, Tree<String>> computeTree(DataInput dataInput, Map<Integer, InvokeMetadata> idToMetadata, TraceResultConfig rsConfig) throws Exception {
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

        Tree<String> rsTree = null;
        if (tree.hasChild()) {
            TreeUtils.traverse(
                    tree,
                    node -> {
                        removeDuplicatedThrow(node);
                        node.reverseChildren();
                    }
            );
            rsTree = new TraceRsTreeConverter(valueCache).convertTree(tree, idToMetadata, rsConfig);
            if (!rsTree.hasChild())
                rsTree = null;
        }
        return new Pair<>(totalSize, rsTree);
    }

    private void printTree(Tree<String> tree) {
        TreeUtils.printTree(
                tree,
                new TreeUtils.PrintConfig(false),
                (node, config) -> node.getData()
        );
        System.out.println("\n\n");
    }

    private void removeDuplicatedThrow(Node<TraceItem> node) {
        if (node.countChildren() > 1) {
            Node<TraceItem> firstChild = node.firstChild();
            TraceItem fcData = firstChild.getData();
            if (fcData.hasError()) {
                while (node.countChildren() > 1) {
                    Node<TraceItem> secChild = node.getChildAt(1);
                    TraceItem secData = secChild.getData();
                    if (secData.getType() == fcData.getType() &&
                            fcData.getError().equals(secData.getError())) {
                        secChild.destroy();
                    } else
                        break;
                }
            }
        }
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
