package agent.builtin.tools.result.filter;

import agent.builtin.tools.result.ResultOptions;
import agent.builtin.tools.result.ResultParams;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.util.Map;
import java.util.Optional;

import static agent.builtin.tools.result.filter.ResultFilterUtils.populateFilter;

public abstract class TreeResultConverter<T, O extends ResultOptions, P extends ResultParams<O>, R> {
    private static final String HIT = "hit";

    protected abstract AbstractResultFilter<T> createFilter();

    protected abstract InvokeMetadata findMetadata(Map<Integer, InvokeMetadata> idToMetadata, T data);

    protected abstract Node<R> createNode(Node<T> node, Map<Integer, InvokeMetadata> idToMetadata, InvokeMetadata metadata, O opts);

    public Tree<R> convert(Node<T> tree, Map<Integer, InvokeMetadata> idToMetadata, P params) {
        AbstractResultFilter<T> filter = createFilter();
        AbstractResultFilter<T> chainFilter = createFilter();
        populateFilter(filter, chainFilter, params.opts);

        Tree<R> rsTree = new Tree<>();
        tree.getChildren()
                .stream()
                .filter(
                        child -> acceptNode(child, idToMetadata, filter)
                )
                .peek(
                        child -> TreeUtils.traverse(
                                child,
                                node -> {
                                    if (acceptNode(node, idToMetadata, chainFilter)) {
                                        Node<T> tmp = node;
                                        while (tmp != tree && tmp.getUserProp(HIT) == null) {
                                            tmp.setUserProp(HIT, 1);
                                            tmp = tmp.getParent();
                                        }
                                    }
                                }
                        )
                )
                .forEach(
                        child -> Optional.ofNullable(
                                convertNode(child, idToMetadata, params.opts)
                        ).ifPresent(rsTree::appendChild)
                );
        return rsTree;
    }

    private boolean acceptNode(Node<T> node, Map<Integer, InvokeMetadata> idToMetadata, AbstractResultFilter<T> filter) {
        InvokeMetadata metadata = findMetadata(
                idToMetadata,
                node.getData()
        );
        return filter.accept(
                new ResultFilterData<>(
                        metadata,
                        node.getData(),
                        node.getLevel()
                )
        );
    }

    private Node<R> convertNode(Node<T> node, Map<Integer, InvokeMetadata> idToMetadata, O opts) {
        if (node.getUserProp(HIT) == null)
            return null;
        InvokeMetadata metadata = findMetadata(
                idToMetadata,
                node.getData()
        );
        Node<R> rsNode = createNode(node, idToMetadata, metadata, opts);
        node.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(child, idToMetadata, opts)
                ).ifPresent(rsNode::appendChild)
        );
        return rsNode;
    }


}
