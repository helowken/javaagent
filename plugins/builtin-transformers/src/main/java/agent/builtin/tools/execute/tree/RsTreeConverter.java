package agent.builtin.tools.execute.tree;

import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.util.Map;
import java.util.Optional;

public abstract class RsTreeConverter<T, V, P> {
    protected abstract InvokeMetadata findMetadata(Map<Integer, InvokeMetadata> idToMetadata, V data);

    protected abstract Node<T> createNode(Node<V> node, Map<Integer, InvokeMetadata> idToMetadata, InvokeMetadata pnMetadata, P config);

    public Tree<T> convertTree(Tree<V> tree, Map<Integer, InvokeMetadata> idToMetadata, P config) {
        Tree<T> rsTree = new Tree<>();
        tree.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(child, idToMetadata, config)
                ).ifPresent(rsTree::appendChild)
        );
        return rsTree;
    }

    private Node<T> convertNode(Node<V> node, Map<Integer, InvokeMetadata> idToMetadata, P config) {
        InvokeMetadata metadata = findMetadata(
                idToMetadata,
                node.getData()
        );
        Node<T> rsNode = createNode(node, idToMetadata, metadata, config);
        node.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(child, idToMetadata, config)
                ).ifPresent(rsNode::appendChild)
        );
        return rsNode;
    }
}
