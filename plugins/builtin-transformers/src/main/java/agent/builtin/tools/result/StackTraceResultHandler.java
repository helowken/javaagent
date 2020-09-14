package agent.builtin.tools.result;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.TypeObject;
import agent.base.utils.Utils;
import agent.builtin.tools.result.parse.StackTraceResultParams;
import agent.common.struct.DefaultBBuff;
import agent.common.struct.impl.MapStruct;
import agent.common.struct.impl.Structs;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.common.utils.JsonUtils;
import agent.server.command.entity.StackTraceElementEntity;
import agent.server.command.entity.StackTraceEntity;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class StackTraceResultHandler extends AbstractResultHandler<Tree<StackTraceCountItem>, StackTraceResultParams> {
    private static final Logger logger = Logger.getLogger(StackTraceResultHandler.class);

    @Override
    Tree<StackTraceCountItem> calculate(Collection<File> dataFiles, StackTraceResultParams params) {
        AtomicReference<Tree<StackTraceCountItem>> ref = new AtomicReference<>(null);
        dataFiles.parallelStream()
                .map(this::doCalculate)
                .forEach(
                        tree -> {
                            if (!ref.compareAndSet(null, tree))
                                mergeTrees(
                                        ref.get(),
                                        tree
                                );
                        }
                );
        return Optional.ofNullable(
                ref.get()
        ).orElseThrow(
                () -> new RuntimeException("No tree found in local.")
        );
    }

    private Tree<StackTraceCountItem> doCalculate(File dataFile) {
        Tree<StackTraceCountItem> tree = new Tree<>();
        MapStruct<String, Object> struct = Structs.newMap();
        calculateBytesFile(
                dataFile,
                in -> {
                    int totalSize = 0;
                    int size = in.readInt();
                    byte[] bs = new byte[size];
                    IOUtils.read(in, bs);
                    totalSize += Integer.BYTES;
                    totalSize += size;

                    struct.clear();
                    struct.deserialize(
                            new DefaultBBuff(
                                    ByteBuffer.wrap(bs)
                            )
                    );
                    convertStackTraceToTree(
                            tree,
                            JsonUtils.convert(
                                    struct.getAll(),
                                    new TypeObject<StackTraceEntity>() {
                                    }
                            )
                    );

                    return totalSize;
                }
        );
        return tree;
    }

    private void convertStackTraceToTree(Tree<StackTraceCountItem> tree, StackTraceEntity entity) {
        String name = entity.getThreadName() + "-" + entity.getThreadId();
        Node<StackTraceCountItem> node = getOrCreateNode(tree, name);
        List<StackTraceElementEntity> els = entity.getStackTraceElements();
        if (els != null) {
            Collections.reverse(els);
            for (StackTraceElementEntity el : els) {
                String elName = formatClassName(el.getClassName()) + ":" + el.getMethodName();
                node = getOrCreateNode(node, elName);
            }
        }
    }

    private Node<StackTraceCountItem> getOrCreateNode(Node<StackTraceCountItem> node, String name) {
        Node<StackTraceCountItem> childNode = node.findFirstChild(
                item -> item.name.equals(name)
        );
        if (childNode == null)
            childNode = node.appendChild(
                    new Node<>(
                            new StackTraceCountItem(name)
                    )
            );
        else
            childNode.getData().increase();
        return childNode;
    }

    private void mergeTrees(Tree<StackTraceCountItem> sumTree, Tree<StackTraceCountItem> tree) {
        tree.getChildren().forEach(
                child -> mergeNodes(sumTree, child)
        );
    }

    private void mergeNodes(Node<StackTraceCountItem> pn, Node<StackTraceCountItem> newChild) {
        Node<StackTraceCountItem> oldChild = pn.findFirstChild(
                item -> item.name.equals(
                        newChild.getData().name
                )
        );
        if (oldChild == null)
            pn.appendChild(newChild);
        else {
            oldChild.getData().merge(
                    newChild.getData()
            );
            for (Node<StackTraceCountItem> cn : newChild.getChildren()) {
                mergeNodes(oldChild, cn);
            }
        }
    }

    @Override
    public void exec(StackTraceResultParams params) throws Exception {
        logger.debug("Params: {}", params);
        String inputPath = params.getInputPath();
        List<File> dataFiles = findDataFiles(inputPath);
        Tree<StackTraceCountItem> tree = calculateStats(dataFiles, params);
        IOUtils.writeToConsole(
                writer -> convertToFlameGraphData(tree).forEach(
                        line -> Utils.wrapToRtError(
                                () -> {
                                    writer.write(line);
                                    writer.append('\n');
                                }
                        )
                )
        );
    }

    private Collection<String> convertToFlameGraphData(Tree<StackTraceCountItem> tree) {
        Set<String> rs = new TreeSet<>();
        tree.getChildren().forEach(
                subTree -> TreeUtils.traverse(
                        subTree,
                        node -> {
                            if (!node.hasChild())
                                rs.add(
                                        getFullPath(node) + " " + node.getData().count
                                );
                        }
                )
        );
        return rs;
    }

    private String getFullPath(Node<StackTraceCountItem> node) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        Node<StackTraceCountItem> tmp = node;
        while (tmp != null && tmp.getData() != null) {
            if (count > 0)
                sb.insert(0, ';');
            sb.insert(0, tmp.getData().name);
            ++count;
            tmp = tmp.getParent();
        }
        return sb.toString();
    }

    private String formatClassName(String className) {
        return 'L' + className.replaceAll("\\.", "/");
    }

}
