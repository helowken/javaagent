package agent.builtin.tools.result;

import agent.base.args.parse.Opts;
import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.builtin.tools.result.parse.StackTraceResultOptConfigs;
import agent.builtin.tools.result.parse.StackTraceResultParams;
import agent.common.args.parse.FilterOptUtils;
import agent.common.args.parse.StackTraceOptConfigs;
import agent.common.config.StringFilterConfig;
import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructContext;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.server.command.entity.StackTraceElementEntity;
import agent.server.command.entity.StackTraceEntity;
import agent.server.command.executor.StackTraceUtils;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.FilterUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unchecked")
public class StackTraceResultHandler extends AbstractResultHandler<Tree<StackTraceCountItem>, StackTraceResultParams> {
    private static final Logger logger = Logger.getLogger(StackTraceResultHandler.class);
    private static final String CLASS_METHOD_SEP = ":";
    private static final String OUTPUT_COST_TIME = "costTime";
    private static final String OUTPUT_FLAME_GRAPH = "flameGraph";
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
        AgentFilter<String> elementFilter = newFilter(
                StackTraceOptConfigs.getElementExpr(opts)
        );
        AgentFilter<String> stackFilter = newFilter(
                StackTraceOptConfigs.getStackExpr(opts)
        );
        AgentFilter<String> threadFilter = newFilter(
                StackTraceOptConfigs.getThreadExpr(opts)
        );
        Map<Integer, String> metadata = params.getMetadata();
        Tree<StackTraceCountItem> tree = new Tree<>();
        calculateBinaryFile(
                dataFile,
                in -> deserializeBytes(
                        in,
                        bb -> convertStackTraceToTree(
                                tree,
                                StackTraceUtils.getStackTraces(
                                        Struct.deserialize(bb, context),
                                        entity -> metadata.get(entity.getNameId()),
                                        stEl -> metadata.get(stEl.getClassId()) + ": " + metadata.get(stEl.getMethodId()),
                                        threadFilter,
                                        elementFilter,
                                        stackFilter
                                ),
                                metadata,
                                params
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

    private void convertStackTraceToTree(Tree<StackTraceCountItem> tree, Map<StackTraceEntity, List<StackTraceElementEntity>> stMap,
                                         Map<Integer, String> metadata, StackTraceResultParams params) {
        stMap.forEach(
                (entity, els) -> {
                    if (!els.isEmpty()) {
                        boolean merge = StackTraceResultOptConfigs.isMerge(
                                params.getOpts()
                        );
                        Collections.reverse(els);
                        String name;
                        if (merge) {
                            StackTraceElementEntity el = els.get(0);
                            els.remove(0);
                            name = getElementName(el, metadata);
                        } else {
                            name = metadata.get(
                                    entity.getNameId()
                            ) + "-" + entity.getThreadId();
                        }

                        Node<StackTraceCountItem> node = getOrCreateNode(tree, name);
                        if (els.isEmpty())
                            node.getData().increase();
                        else {
                            for (int i = 0, len = els.size(); i < len; ++i) {
                                StackTraceElementEntity el = els.get(i);
                                node = getOrCreateNode(
                                        node,
                                        getElementName(el, metadata)
                                );
                                if (i == len - 1) {
                                    node.getData().increase();
                                }
                            }
                        }
                    }
                }
        );
    }

    private String getElementName(StackTraceElementEntity el, Map<Integer, String> metadata) {
        return formatClassName(
                metadata.get(
                        el.getClassId()
                )
        ) + CLASS_METHOD_SEP + metadata.get(
                el.getMethodId()
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

        String outputFormat = StackTraceResultOptConfigs.getOutputFormat(params.getOpts());
        if (outputFormat == null)
            outputFormat = OUTPUT_FLAME_GRAPH;
        switch (outputFormat) {
            case OUTPUT_COST_TIME:
                outputCostTimeData(tree, params);
                break;
            case OUTPUT_FLAME_GRAPH:
                outputFlameGraph(tree);
                break;
            default:
                throw new Exception("Invalid output format: " + outputFormat);
        }
    }

    private void outputCostTimeData(Tree<StackTraceCountItem> tree, StackTraceResultParams params) throws Exception {
        AtomicInteger total = new AtomicInteger(0);
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceCountItem data = node.getData();
                    if (data != null && data.isValid())
                        total.set(total.get() + data.count);
                }
        );
        int totalCount = total.get();
        float rate = StackTraceResultOptConfigs.getRate(
                params.getOpts()
        );
        Map<String, Set<String>> classToMethods = new HashMap<>();
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceCountItem data = node.getData();
                    if (data != null &&
                            data.isValid() &&
                            ((float) data.count / totalCount) >= rate) {
                        String[] parts = data.name.split(CLASS_METHOD_SEP);
                        if (parts.length == 2) {
                            classToMethods.computeIfAbsent(
                                    parts[0],
                                    clazz -> new HashSet<>()
                            ).add(parts[1]);
                        }
                    }
                }
        );
        if (classToMethods.isEmpty())
            System.out.println("No class and method matched.");
        else
            IOUtils.writeToConsole(
                    writer -> classToMethods.forEach(
                            (clazz, methods) -> Utils.wrapToRtError(
                                    () -> {
                                        writer.write(clazz + ": " + methods);
                                        writer.append('\n');
                                    }
                            )
                    )
            );
    }

    private void outputFlameGraph(Tree<StackTraceCountItem> tree) throws Exception {
        IOUtils.writeToConsole(
                writer -> convertToFlameGraphData(tree)
                        .forEach(
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
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceCountItem data = node.getData();
                    if (data != null && data.isValid()) {
                        rs.add(
                                getFullPath(node) + " " + node.getData().count
                        );
                    }
                }
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
        return className.replaceAll("\\.", "/");
    }

}
