package bstmap;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/** Tests by Brendan Hu, Spring 2015, revised for 2016 by Josh Hug */
public class TestBSTMap {

  	@Test
    public void sanityGenericsTest() {
    	try {
    		BSTMap<String, String> a = new BSTMap<String, String>();
	    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
	    	BSTMap<Integer, String> c = new BSTMap<Integer, String>();
	    	BSTMap<Boolean, Integer> e = new BSTMap<Boolean, Integer>();
	    } catch (Exception e) {
	    	fail();
	    }
    }

    //assumes put/size/containsKey/get work
    @Test
    public void sanityClearTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        for (int i = 0; i < 455; i++) {
            b.put("hi" + i, 1+i);
            //make sure put is working via containsKey and get
            assertTrue( null != b.get("hi" + i) && (b.get("hi"+i).equals(1+i))
                        && b.containsKey("hi" + i));
        }
        assertEquals(455, b.size());
        b.clear();
        assertEquals(0, b.size());
        for (int i = 0; i < 455; i++) {
            assertTrue(null == b.get("hi" + i) && !b.containsKey("hi" + i));
        }
    }

    // assumes put works
    @Test
    public void sanityContainsKeyTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        assertFalse(b.containsKey("waterYouDoingHere"));
        b.put("waterYouDoingHere", 0);
        assertTrue(b.containsKey("waterYouDoingHere"));
    }

    // assumes put works
    @Test
    public void sanityGetTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        assertEquals(null,b.get("starChild"));
        assertEquals(0, b.size());
        b.put("starChild", 5);
        assertTrue(((Integer) b.get("starChild")).equals(5));
        b.put("KISS", 5);
        assertTrue(((Integer) b.get("KISS")).equals(5));
        assertNotEquals(null,b.get("starChild"));
        assertEquals(2, b.size());
    }

    // assumes put works
    @Test
    public void sanitySizeTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        assertEquals(0, b.size());
        b.put("hi", 1);
        assertEquals(1, b.size());
        for (int i = 0; i < 455; i++)
            b.put("hi" + i, 1);
        assertEquals(456, b.size());
    }

    //assumes get/containskey work
    @Test
    public void sanityPutTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        b.put("hi", 1);
        assertTrue(b.containsKey("hi") && b.get("hi") != null);
    }

    //assumes put works
    @Test
    public void containsKeyNullTest() {
        BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        b.put("hi", null);
        assertTrue(b.containsKey("hi"));
    }

    // ==================== REMOVE TESTS ====================

    @Test
    public void removeLeafNode() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("m", 10);
        b.put("a", 5);
        b.put("z", 15);
        
        assertEquals(3, b.size());
        Integer removed = b.remove("a");
        assertEquals(5, (int) removed);
        assertEquals(2, b.size());
        assertFalse(b.containsKey("a"));
        assertTrue(b.containsKey("m"));
        assertTrue(b.containsKey("z"));
        
        removed = b.remove("z");
        assertEquals(15, (int) removed);
        assertEquals(1, b.size());
        assertFalse(b.containsKey("z"));
    }

    @Test
    public void removeNodeWithOneChild() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("m", 10);
        b.put("a", 5);
        b.put("b", 7);  // right child of "a"
        
        assertEquals(3, b.size());
        Integer removed = b.remove("a");
        assertEquals(5, (int) removed);
        assertEquals(2, b.size());
        assertFalse(b.containsKey("a"));
        assertTrue(b.containsKey("b"));
        assertEquals(7, (int) b.get("b"));
        assertTrue(b.containsKey("m"));
    }

    @Test
    public void removeNodeWithTwoChildren() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("m", 10);
        b.put("a", 5);
        b.put("z", 15);
        b.put("k", 8);   // left child of "m"
        b.put("n", 12);  // left child of "z"
        
        // Remove "m" (root with two children)
        // Successor should be "n" (smallest in right subtree)
        Integer removed = b.remove("m");
        assertEquals(10, (int) removed);
        assertEquals(4, b.size());
        assertFalse(b.containsKey("m"));
        assertTrue(b.containsKey("n"));
        assertEquals(12, (int) b.get("n"));
        assertTrue(b.containsKey("a"));
        assertTrue(b.containsKey("z"));
        assertTrue(b.containsKey("k"));
    }

    @Test
    public void removeRootNode() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("m", 10);
        b.put("a", 5);
        b.put("z", 15);
        
        // Remove root with two children
        Integer removed = b.remove("m");
        assertEquals(10, (int) removed);
        assertEquals(2, b.size());
        assertFalse(b.containsKey("m"));
        
        // New root should be "z" (successor)
        assertEquals(15, (int) b.get("z"));
        assertEquals(5, (int) b.get("a"));
    }

    @Test
    public void removeRootWithOnlyLeftChild() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("m", 10);
        b.put("a", 5);
        
        Integer removed = b.remove("m");
        assertEquals(10, (int) removed);
        assertEquals(1, b.size());
        assertEquals(5, (int) b.get("a"));
        assertFalse(b.containsKey("m"));
    }

    @Test
    public void removeRootWithOnlyRightChild() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("m", 10);
        b.put("z", 15);
        
        Integer removed = b.remove("m");
        assertEquals(10, (int) removed);
        assertEquals(1, b.size());
        assertEquals(15, (int) b.get("z"));
        assertFalse(b.containsKey("m"));
    }

    @Test
    public void removeNonExistentKey() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("a", 1);
        
        assertNull(b.remove("nonexistent"));
        assertEquals(1, b.size());
        assertTrue(b.containsKey("a"));
    }

    @Test
    public void removeFromEmptyMap() {
        BSTMap<String, Integer> b = new BSTMap<>();
        assertNull(b.remove("a"));
        assertEquals(0, b.size());
    }

    @Test
    public void removeWithValueCheck() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("a", 1);
        b.put("b", 2);
        b.put("c", 3);
        
        // Value matches - should remove
        assertEquals(2, (int) b.remove("b", 2));
        assertEquals(2, b.size());
        assertFalse(b.containsKey("b"));
        
        // Value doesn't match - should not remove
        assertNull(b.remove("a", 999));
        assertEquals(2, b.size());
        assertTrue(b.containsKey("a"));
        assertEquals(1, (int) b.get("a"));
        
        // Null value handling
        b.put("d", null);
        assertNull(b.remove("d", null));
        assertEquals(2, b.size());
        assertFalse(b.containsKey("d"));
    }

    @Test
    public void removeUpdatesSizeCorrectly() {
        BSTMap<Integer, String> b = new BSTMap<>();
        for (int i = 0; i < 100; i++) {
            b.put(i, "val" + i);
        }
        assertEquals(100, b.size());
        
        for (int i = 0; i < 50; i++) {
            b.remove(i);
            assertEquals(99 - i, b.size());
        }
        
        for (int i = 50; i < 100; i++) {
            b.remove(i);
            assertEquals(99 - i, b.size());
        }
        assertEquals(0, b.size());
    }

    @Test
    public void removeComplexTree() {
        // Build a more complex tree
        BSTMap<Integer, String> b = new BSTMap<>();
        int[] keys = {50, 30, 70, 20, 40, 60, 80, 10, 25, 35, 45, 55, 65, 75, 90};
        for (int k : keys) {
            b.put(k, "v" + k);
        }
        assertEquals(15, b.size());
        
        // Remove leaf
        b.remove(10);
        assertEquals(14, b.size());
        assertFalse(b.containsKey(10));
        
        // Remove node with one child (20 -> 25)
        b.remove(20);
        assertEquals(13, b.size());
        assertFalse(b.containsKey(20));
        assertTrue(b.containsKey(25));
        
        // Remove node with two children (30)
        b.remove(30);
        assertEquals(12, b.size());
        assertFalse(b.containsKey(30));
        assertTrue(b.containsKey(35));
        
        // Remove root (50)
        b.remove(50);
        assertEquals(11, b.size());
        assertFalse(b.containsKey(50));
        assertTrue(b.containsKey(55)); // successor
        
        // Verify remaining keys
        for (int k : keys) {
            if (k != 10 && k != 20 && k != 30 && k != 50) {
                assertTrue("Missing key: " + k, b.containsKey(k));
            }
        }
    }

    @Test
    public void removeThenReinsert() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("a", 1);
        b.put("b", 2);
        b.put("c", 3);
        
        b.remove("b");
        b.put("b", 20);  // Reinsert with different value
        
        assertEquals(3, b.size());
        assertEquals(20, (int) b.get("b"));
    }

    // ==================== ITERATOR TESTS ====================

    @Test
    public void iteratorInOrder() {
        BSTMap<Integer, String> b = new BSTMap<>();
        b.put(50, "f");
        b.put(30, "d");
        b.put(70, "h");
        b.put(20, "c");
        b.put(40, "e");
        b.put(60, "g");
        b.put(80, "i");
        
        List<Integer> keys = new ArrayList<>();
        for (Integer k : b) {
            keys.add(k);
        }
        
        List<Integer> expected = Arrays.asList(20, 30, 40, 50, 60, 70, 80);
        assertEquals(expected, keys);
    }

    @Test
    public void iteratorEmptyMap() {
        BSTMap<String, Integer> b = new BSTMap<>();
        Iterator<String> it = b.iterator();
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void iteratorThrowsOnEmpty() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.iterator().next();
    }

    @Test
    public void iteratorAfterModification() {
        BSTMap<Integer, String> b = new BSTMap<>();
        b.put(2, "b");
        b.put(1, "a");
        b.put(3, "c");
        
        Iterator<Integer> it = b.iterator();
        assertEquals((Integer) 1, it.next());
        
        b.put(0, "z");  // Modify during iteration
        
        // Iterator behavior after modification is undefined,
        // but should not crash
        try {
            while (it.hasNext()) {
                it.next();
            }
        } catch (Exception e) {
            // Acceptable
        }
    }

    @Test
    public void iteratorMultipleIterators() {
        BSTMap<Integer, String> b = new BSTMap<>();
        b.put(2, "b");
        b.put(1, "a");
        b.put(3, "c");
        
        Iterator<Integer> it1 = b.iterator();
        Iterator<Integer> it2 = b.iterator();
        
        assertEquals((Integer) 1, it1.next());
        assertEquals((Integer) 1, it2.next());
        assertEquals((Integer) 2, it1.next());
        assertEquals((Integer) 2, it2.next());
        assertEquals((Integer) 3, it1.next());
        assertEquals((Integer) 3, it2.next());
        assertFalse(it1.hasNext());
        assertFalse(it2.hasNext());
    }

    @Test
    public void iteratorAfterRemove() {
        BSTMap<Integer, String> b = new BSTMap<>();
        b.put(2, "b");
        b.put(1, "a");
        b.put(3, "c");
        
        b.remove(2);
        
        List<Integer> keys = new ArrayList<>();
        for (Integer k : b) {
            keys.add(k);
        }
        assertEquals(Arrays.asList(1, 3), keys);
    }

    // ==================== KEYSET TESTS ====================

    @Test
    public void keySetBasic() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("c", 3);
        b.put("a", 1);
        b.put("b", 2);
        
        Set<String> keys = b.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
        assertTrue(keys.contains("c"));
    }

    @Test
    public void keySetEmptyMap() {
        BSTMap<String, Integer> b = new BSTMap<>();
        Set<String> keys = b.keySet();
        assertTrue(keys.isEmpty());
    }

    @Test
    public void keySetIndependent() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("a", 1);
        
        Set<String> keys1 = b.keySet();
        b.put("b", 2);
        Set<String> keys2 = b.keySet();
        
        assertEquals(1, keys1.size());  // Old set unchanged
        assertEquals(2, keys2.size());  // New set has both
    }

    @Test
    public void keySetAfterRemove() {
        BSTMap<Integer, String> b = new BSTMap<>();
        b.put(1, "a");
        b.put(2, "b");
        b.put(3, "c");
        
        Set<Integer> keys = b.keySet();
        b.remove(2);
        
        assertEquals(3, keys.size());  // Old set unchanged
        assertEquals(2, b.keySet().size());  // New set reflects removal
    }

    // ==================== PUT TESTS ====================

    @Test
    public void putUpdatesExistingKey() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("a", 1);
        assertEquals(1, b.size());
        
        b.put("a", 100);  // Update
        assertEquals(1, b.size());
        assertEquals(100, (int) b.get("a"));
    }

    @Test
    public void putNullValue() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("a", null);
        assertTrue(b.containsKey("a"));
        assertNull(b.get("a"));
        assertEquals(1, b.size());
    }

    @Test
    public void putNullKey() {
        BSTMap<String, Integer> b = new BSTMap<>();
        // First null key should throw NPE
        try {
            b.put(null, 1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        // Other operations with null key should also throw
        try {
            b.get(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {}
        try {
            b.containsKey(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {}
        try {
            b.remove(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {}
    }

    // ==================== CLEAR TESTS ====================

    @Test
    public void clearEmptyMap() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.clear();
        assertEquals(0, b.size());
        assertNull(b.get("a"));
    }

    @Test
    public void clearThenReuse() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("a", 1);
        b.put("b", 2);
        b.clear();
        
        assertEquals(0, b.size());
        b.put("c", 3);
        assertEquals(1, b.size());
        assertEquals(3, (int) b.get("c"));
        assertFalse(b.containsKey("a"));
    }

    // ==================== STRESS/EDGE CASE TESTS ====================

    @Test
    public void insertInOrderCreatesRightSkewedTree() {
        BSTMap<Integer, Integer> b = new BSTMap<>();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            b.put(i, i * 2);
        }
        assertEquals(n, b.size());
        
        for (int i = 0; i < n; i++) {
            assertEquals(i * 2, (int) b.get(i));
        }
        
        // Remove all in order
        for (int i = 0; i < n; i++) {
            b.remove(i);
            assertEquals(n - i - 1, b.size());
        }
        assertEquals(0, b.size());
    }

    @Test
    public void insertReverseOrderCreatesLeftSkewedTree() {
        BSTMap<Integer, Integer> b = new BSTMap<>();
        int n = 1000;
        for (int i = n - 1; i >= 0; i--) {
            b.put(i, i * 2);
        }
        assertEquals(n, b.size());
        
        for (int i = 0; i < n; i++) {
            assertEquals(i * 2, (int) b.get(i));
        }
    }

    @Test
    public void randomInsertAndRemove() {
        BSTMap<Integer, Integer> b = new BSTMap<>();
        int n = 500;
        List<Integer> keys = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            keys.add(i);
        }
        Collections.shuffle(keys);
        
        // Random insert
        for (int k : keys) {
            b.put(k, k * 10);
        }
        assertEquals(n, b.size());
        
        Collections.shuffle(keys);
        // Random remove
        for (int k : keys) {
            b.remove(k);
        }
        assertEquals(0, b.size());
    }

    @Test
    public void duplicateKeysDifferentValues() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("key", 1);
        b.put("key", 2);
        b.put("key", 3);
        
        assertEquals(1, b.size());
        assertEquals(3, (int) b.get("key"));
    }

    @Test
    public void getNonExistentKey() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("a", 1);
        assertNull(b.get("nonexistent"));
    }

    @Test
    public void containsKeyNonExistent() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("a", 1);
        assertFalse(b.containsKey("nonexistent"));
    }

    @Test
    public void integerKeysNaturalOrder() {
        BSTMap<Integer, String> b = new BSTMap<>();
        b.put(5, "e");
        b.put(-3, "a");
        b.put(0, "c");
        b.put(10, "f");
        b.put(-10, "b");
        
        List<Integer> keys = new ArrayList<>();
        for (Integer k : b) {
            keys.add(k);
        }
        assertEquals(Arrays.asList(-10, -3, 0, 5, 10), keys);
    }

    @Test
    public void stringKeysLexicographicOrder() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("zebra", 1);
        b.put("apple", 2);
        b.put("banana", 3);
        b.put("cherry", 4);
        b.put("date", 5);
        
        List<String> keys = new ArrayList<>();
        for (String k : b) {
            keys.add(k);
        }
        assertEquals(Arrays.asList("apple", "banana", "cherry", "date", "zebra"), keys);
    }

}