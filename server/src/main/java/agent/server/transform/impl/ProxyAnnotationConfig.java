package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.server.transform.tools.asm.annotation.OnAfter;
import agent.server.transform.tools.asm.annotation.OnBefore;
import agent.server.transform.tools.asm.annotation.OnReturning;
import agent.server.transform.tools.asm.annotation.OnThrowing;

import java.lang.reflect.Method;

import static agent.server.transform.tools.asm.ProxyArgsMask.*;

public abstract class ProxyAnnotationConfig<T> {
    private static final Logger logger = Logger.getLogger(ProxyAnnotationConfig.class);
    public static final int ARGS_NONE = -1;
    public static final int ARGS_ON_BEFORE = 1;
    public static final int ARGS_ON_RETURNING = 2;
    public static final int ARGS_ON_THROWING = 3;
    public static final int ARGS_ON_AFTER = 4;

    private final ThreadLocal<Node<T>> local = new ThreadLocal<>();

    @OnBefore(mask = DEFAULT_BEFORE | MASK_METHOD, argsHint = ARGS_ON_BEFORE)
    public void onBefore(Object[] args, Class<?>[] argTypes, Method method, Object... otherArgs) {
        Node<T> currNode = local.get();
        Node<T> newNode = new Node<>(
                newData(currNode, args, argTypes, method, otherArgs)
        );
        if (currNode != null) {
            newNode.previous = currNode;
            currNode.next = newNode;
        }
        local.set(newNode);
        processOnBefore(currNode, args, argTypes, method, otherArgs);
    }

    @OnReturning(mask = DEFAULT_ON_RETURNING | MASK_METHOD, argsHint = ARGS_ON_RETURNING)
    public void onReturning(Object returnValue, Class<?> returnType, Method method, Object... otherArgs) {
        Node<T> currNode = getNode("returning", method);
        if (currNode != null)
            processOnReturning(currNode, returnValue, returnType, method, otherArgs);
    }

    @OnThrowing(mask = DEFAULT_ON_THROWING | MASK_METHOD, argsHint = ARGS_ON_THROWING)
    public void onThrowing(Throwable error, Method method, Object... otherArgs) {
        Node<T> currNode = getNode("throwing", method);
        if (currNode != null)
            processOnThrowing(currNode, error, method, otherArgs);
    }

    @OnAfter(mask = MASK_METHOD, argsHint = ARGS_ON_AFTER)
    private void onAfter(Method method, Object... args) {
        Node<T> currNode = getNode("after", method);
        if (currNode != null) {
            processOnAfter(currNode, method, args);
            Node<T> preNode = currNode.previous;
            if (preNode == null)
                local.remove();
            else {
                preNode.next = null;
                currNode.previous = null;
                local.set(preNode);
            }
        }
    }

    private Node<T> getNode(String stage, Method method) {
        Node<T> currNode = local.get();
        if (currNode == null)
            logger.error("No node found on {} for method: {}", stage, method);
        return currNode;
    }

    protected T newData(Node<T> preNode, Object[] args, Class<?>[] argTypes, Method method, Object[] otherArgs) {
        return null;
    }

    protected void processOnBefore(Node<T> currNode, Object[] args, Class<?>[] argTypes, Method method, Object[] otherArgs) {
    }

    protected void processOnReturning(Node<T> currNode, Object returnValue, Class<?> returnType, Method method, Object[] otherArgs) {
    }

    protected void processOnThrowing(Node<T> currNode, Throwable error, Method method, Object[] otherArgs) {
    }

    protected void processOnAfter(Node<T> currNode, Method method, Object[] otherArgs) {
    }

    public static class Node<T> {
        private final T data;
        private Node<T> previous;
        private Node<T> next;

        private Node(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public Node<T> getPrevious() {
            return previous;
        }

        public Node<T> getNext() {
            return next;
        }

        public int size() {
            int size = 1;
            Node<T> preNode = previous;
            while (preNode != null) {
                ++size;
                preNode = preNode.previous;
            }
            return size;
        }

        public boolean isRoot() {
            return previous == null;
        }
    }
}
