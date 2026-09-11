package bstmap;

import java.util.Set;
import java.util.Stack;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>, Iterable<K> {
    private class BSTNode {
        K key;
        V val;
        BSTNode left;
        BSTNode right;

        public BSTNode(K key, V val) {
            this.key = key;
            this.val = val;
        }
    }

    private class FindResult {
        BSTNode target;
        BSTNode parent;

        public FindResult(BSTNode target, BSTNode parent) {
            this.target = target;
            this.parent = parent;
        }
    }

    private BSTNode root;
    private int size;

    public BSTMap() {}

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        addKey(root, keys);
        return keys;
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new NullPointerException();
        }
        FindResult result = findNode(key);
        return result.target != null;
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new NullPointerException();
        }
        if (this.size == 0) {
            root = new BSTNode(key, value);
            size++;
            return;
        }
        FindResult result = findNode(key);
        if (result.target != null) {
            result.target.val = value;
        } else {
            size++;
            if (key.compareTo(result.parent.key) < 0) {
                result.parent.left = new BSTNode(key, value);
            } else {
                result.parent.right = new BSTNode(key, value);
            }
        }
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new NullPointerException();
        }
        FindResult result = findNode(key);
        return result.target != null ? result.target.val : null;
    }

    @Override
    public V remove(K key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return remove(key, null, false);
    }

    @Override
    public V remove(K key, V value) {
        if (key == null) {
            throw new NullPointerException();
        }
        return remove(key, value, true);
    }

    public void printInOrder() {
        printNode(root);
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTIterator();
    }

    private class BSTIterator implements Iterator<K> {
        private Stack<BSTNode> stack;

        public BSTIterator() {
            stack = new Stack<>();
            pushLeft(root);
        }

        @Override
        public boolean hasNext() {
            return !stack.empty();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            BSTNode node = stack.pop();
            pushLeft(node.right);
            return node.key;
        }

        private void pushLeft(BSTNode root) {
            while (root != null) {
                stack.push(root);
                root = root.left;
            }
        }
    }

    private V remove(K key, V value, boolean check) {
        FindResult result = findNode(key);
        if (result.target == null) {
            return null;
        }
        if (check && !Objects.equals(value, result.target.val)) {
            return null;
        }

        size--;
        BSTNode replace = getReplace(result.target);
        
        if (result.parent == null) {
            root = replace;
        } else if (result.parent.left == result.target) {
            result.parent.left = replace;
        } else {
            result.parent.right = replace;
        }

        return result.target.val;
    }

    private void addKey(BSTNode root, Set<K> keys) {
        if (root == null) {
            return;
        }
        keys.add(root.key);
        addKey(root.left, keys);
        addKey(root.right, keys);
    }

    private void printNode(BSTNode root) {
        if (root == null) {
            return;
        }
        printNode(root.left);
        System.out.println("Key: " + root.key + ", Value: " + root.val);
        printNode(root.right);
    }

    private FindResult findNode(K key) {
        BSTNode ptr = root;
        BSTNode parent = null;
        while (ptr != null) {
            if (key.compareTo(ptr.key) < 0) {
                parent = ptr;
                ptr = ptr.left;
            } else if (key.compareTo(ptr.key) > 0) {
                parent = ptr;
                ptr = ptr.right;
            } else {
                break;
            }
        }
        return new FindResult(ptr, parent);
    }

    private BSTNode getReplace(BSTNode node) {
        if (node.left == null) {
            return node.right;
        }
        if (node.right == null) {
            return node.left;
        }

        FindResult successor = findSuccessor(node);
        if (successor.parent == null) {
            node.right = successor.target.right;
        } else {
            successor.parent.left = successor.target.right;
        }
        successor.target.left = node.left;
        successor.target.right = node.right;

        return successor.target;
    }

    private FindResult findSuccessor(BSTNode root) {
        BSTNode parent = null;
        BSTNode node = root.right;
        while (node.left != null) {
            parent = node;
            node = node.left;
        }
        return new FindResult(node, parent);
    }
}
