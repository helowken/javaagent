package agent.builtin.tools.result;

import agent.base.args.parse.Opts;
import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.builtin.tools.result.parse.StackTraceOptConfigs;
import agent.builtin.tools.result.parse.StackTraceResultParams;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.StringFilterConfig;
import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructContext;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.server.command.entity.StackTraceElementEntity;
import agent.server.command.entity.StackTraceEntity;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.FilterUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unchecked")
public class StackTraceResultHandler extends AbstractResultHandler<Tree<StackTraceCountItem>, StackTraceResultParams> {
    private static final Logger logger = Logger.getLogger(StackTraceResultHandler.class);
    private static final StructContext context = new StructContext();

    static {
        context.setPojoCreator(
                type -> {
                    switch (type) {
                        case StackTraceEntity.TYPE:
                            return new StackTraceEntity();
                        case StackTraceElementEntity.TYPE:
                            return new StackTraceElementEntity();
                        default:
                            return null;
                    }
                }
        );
    }

    @Override
    Map<Integer, String> readMetadata(String inputPath) {
        try {
            Map<String, Integer> nameToId = super.readMetadata(inputPath);
            Map<Integer, String> idToName = new HashMap<>();
            nameToId.forEach(
                    (name, id) -> idToName.put(id, name)
            );
            return idToName;
        } catch (Exception e) {
            logger.error("Read metadata failed.", e);
            return Collections.emptyMap();
        }
    }

    @Override
    Tree<StackTraceCountItem> calculate(Collection<File> dataFiles, StackTraceResultParams params) {
        AtomicReference<Tree<StackTraceCountItem>> ref = new AtomicReference<>(null);
        dataFiles.parallelStream()
                .map(
                        dataFile -> doCalculate(dataFile, params)
                ).forEach(
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

    private Tree<StackTraceCountItem> doCalculate(File dataFile, StackTraceResultParams params) {
        Opts opts = params.getOpts();
        AgentFilter<String> stackFilter = newFilter(
                StackTraceOptConfigs.getStackExpr(opts)
        );
        AgentFilter<String> classFilter = newFilter(
                StackTraceOptConfigs.getElementClassExpr(opts)
        );
        AgentFilter<String> methodFilter = newFilter(
                StackTraceOptConfigs.getElementMethodExpr(opts)
        );
        Map<Integer, String> metadata = params.getMetadata();
        Tree<StackTraceCountItem> tree = new Tree<>();
        calculateBinaryFile(
                dataFile,
                in -> deserializeBytes(
                        in,
                        bb -> convertStackTraceToTree(
                                tree,
                                filterStackTrace(
                                        Struct.deserialize(bb, context),
                                        stackFilter,
                                        classFilter,
                                        methodFilter,
                                        metadata
                                ),
                                metadata
                        )
                )
        );
        return tree;
    }

    private AgentFilter<String> newFilter(String s) {
        if (Utils.isNotBlank(s)) {
            StringFilterConfig filterConfig = FilterOptUtils.newStringFilterConfig(s);
            if (filterConfig != null)
                return FilterUtils.newStringFilter(filterConfig);
        }
        return null;
    }

    private Map<StackTraceEntity, List<StackTraceElementEntity>> filterStackTrace(Map<StackTraceEntity, List<StackTraceElementEntity>> stMap,
                                                                                  AgentFilter<String> stackFilter, AgentFilter<String> classFilter,
                                                                                  AgentFilter<String> methodFilter, Map<Integer, String> metadata) {
        Map<StackTraceEntity, List<StackTraceElementEntity>> rsMap = new HashMap<>();
        stMap.forEach(
                (entity, els) -> {
                    if (els != null && !els.isEmpty()) {
                        boolean flag = false;
                        List<StackTraceElementEntity> rsList = new ArrayList<>();
                        String className;
                        String methodName;
                        for (Object o : els) {
                            StackTraceElementEntity el = (StackTraceElementEntity) o;
                            className = metadata.get(
                                    el.getClassId()
                            );
                            methodName = metadata.get(
                                    el.getMethodId()
                            );
                            if ((classFilter == null || classFilter.accept(className)) &&
                                    (methodFilter == null || methodFilter.accept(methodName)))
                                rsList.add(el);
                            if (stackFilter == null || stackFilter.accept(className))
                                flag = true;
                        }
                        if (flag && !rsList.isEmpty())
                            rsMap.put(entity, rsList);
                    }
                }
        );
        return rsMap;
    }

    private void convertStackTraceToTree(Tree<StackTraceCountItem> tree, Map<StackTraceEntity, List<StackTraceElementEntity>> stMap,
                                         Map<Integer, String> metadata) {
        stMap.forEach(
                (entity, els) -> {
                    String name = metadata.get(
                            entity.getNameId()
                    ) + "-" + entity.getThreadId();
                    Node<StackTraceCountItem> node = getOrCreateNode(tree, name);
                    Collections.reverse(els);
                    for (StackTraceElementEntity el : els) {
                        String elName = formatClassName(
                                metadata.get(
                                        el.getClassId()
                                )
                        ) + ":" + metadata.get(
                                el.getMethodId()
                        );
                        node = getOrCreateNode(node, elName);
                    }
                }
        );
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
        params.setMetadata(
                readMetadata(inputPath)
        );
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
