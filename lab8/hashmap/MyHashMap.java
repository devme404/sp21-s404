package hashmap;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author D
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private double loadFactor;
    private Set<K> keySet;

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        loadFactor = maxLoad;
        keySet = new HashSet<>();
        buckets = createTable(initialSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new HashSet<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < table.length; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    public void clear() {
        keySet.clear();
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
    }

    public boolean containsKey(K key) {
        return keySet.contains(key);
    }

    public V get(K key) {
        if (!containsKey(key)) {
            return null;
        }
        int index = getIndex(key, buckets.length);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    public int size() {
        return keySet.size();
    }

    public Set<K> keySet() {
        return keySet;
    }

    public void put(K key, V value) {
        int index = getIndex(key, buckets.length);
        if (keySet.contains(key)) {
            for (Node n : buckets[index]) {
                if (n.key.equals(key)) {
                    n.value = value;
                    return;
                }
            }
        }

        Node node = createNode(key, value);
        if ((double) size() / (double) buckets.length > loadFactor) {
            resize();
            index = getIndex(key, buckets.length);
        }
        buckets[index].add(node);
        keySet.add(key);
    }

    public V remove(K key) {
        return remove(key, null, false); 
    }

    public V remove(K key, V value) {
        return remove(key, value, true);
    }

    public Iterator<K> iterator() {
        return keySet.iterator();
    }

    private int getIndex(K key, int capacity) {
        int hash = key.hashCode();
        return Math.abs((hash / 23)) % capacity;
    }

    private V remove(K key, V value, boolean toMatch) {
        if (!keySet.contains(key)) {
            return null;
        }
        int index = getIndex(key, buckets.length);
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                if (toMatch && node.value != value) {
                    return null;
                }
                buckets[index].remove(node);
                keySet.remove(key);
                return node.value;
            }
        }
        return null;
    }

    private void resize() {
        Collection<Node>[] newBuckets = createTable(buckets.length * 2);
        for (int i = 0; i < buckets.length; i++) {
            for (Node node : buckets[i]) {
                int index = getIndex(node.key, newBuckets.length);
                newBuckets[index].add(node);
            }
        }
        buckets = newBuckets;
    }
}
