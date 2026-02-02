package deque;

import jh61b.junit.In;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;

public class ArrayDequeTest {
    @Test
    public void addIsEmptySizeTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();

        assertEquals(0, ad.size());
        assertTrue(ad.isEmpty());

        ad.addFirst(1);

        assertFalse(ad.isEmpty());
        assertEquals(1, ad.size());

        ad.addFirst(2);
        assertEquals(2, ad.size());
    }

    @Test
    public void addRemoveTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ad.addFirst(1);
        ad.addFirst(2);

        assertEquals(2, ad.size());

        assertEquals(2, (int) ad.removeFirst());
        assertEquals(1, ad.size());

        assertEquals(1, (int) ad.removeLast());
        assertEquals(0, ad.size());
    }

    @Test
    public void removeEmpty() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();

        assertNull(ad.removeFirst());
        assertNull(ad.removeLast());
    }

    @Test
    public void multiParamTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ArrayDeque<Double> ad2 = new ArrayDeque<>();
        ArrayDeque<String> ad3 = new ArrayDeque<>();
        ArrayDeque<Boolean> ad4 = new ArrayDeque<>();

        ad1.addFirst(1);
        ad2.addFirst(3.1);
        ad3.addFirst("Hello!");
        ad4.addFirst(true);

        assertEquals(1, (int) ad1.removeLast());
        assertEquals(3.1, (double) ad2.removeLast(), 0.0);
        assertEquals("Hello!", (String) ad3.removeLast());
        assertTrue(ad4.removeLast());
    }

    @Test
    public void bigADTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();

        for (int i = 0; i < 1000000; i++) {
            ad.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals(i, (double) ad.removeFirst(), 0.0);
        }

        for (double i = 999999; i >= 500000; i--) {
            assertEquals(i, (double) ad.removeLast(), 0.0);
        }
    }

    @Test
    public void getTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();

        ad.addFirst(1);
        ad.addFirst(2);
        ad.addFirst(3);

        assertEquals(3, (int) ad.get(0));
        assertEquals(2, (int) ad.get(1));
        assertEquals(1, (int) ad.get(2));
    }

    @Test
    public void printDequeTest() {
        ArrayDeque<String> ad = new ArrayDeque<>();
        ad.addLast("Hello");
        ad.addLast(", ");
        ad.addLast("World");
        ad.addLast("!");
        System.out.println("Printing Deque:");
        ad.printDeque();
    }

    @Test
    public void randomizedTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        LinkedListDeque<Integer> lld = new LinkedListDeque<>();

        int N = 5000;
        for (int i = 0; i < N; i++) {
            int operationNumber = StdRandom.uniform(0, 6);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                ad.addLast(randVal);
                lld.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                assertEquals((int) ad.size(), (int) lld.size());
            } else if (operationNumber == 2) {
                // removeLast
                assertEquals(ad.removeLast(), lld.removeLast());
            } else if (operationNumber == 3) {
                // get
                if (ad.size() == 0) {
                    System.out.println("The List is Empty, Skip Testing Get.");
                } else {
                    int index = StdRandom.uniform(0, ad.size());
                    assertEquals((int) ad.get(index), (int) lld.get(index));
                }
            } else if (operationNumber == 4) {
                // addFirst
                int randVal = StdRandom.uniform(0, 100);
                ad.addFirst(randVal);
                lld.addFirst(randVal);
            } else if (operationNumber == 5) {
                // removeFirst
                assertEquals(ad.removeFirst(), lld.removeFirst());
            }
        }
    }

    @Test
    public void iteratorTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();

        ad.addLast(1);
        ad.addLast(2);
        ad.addLast(3);

        int count = 1;
        for (int i : ad) {
            assertEquals(count, i);
            count++;
        }
    }

    @Test
    public void equalsTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ArrayDeque<Integer> ad2 = new ArrayDeque<>();

        ad1.addLast(1);
        ad2.addLast(1);
        ad1.addLast(2);
        ad2.addLast(2);
        ad1.addLast(3);
        ad2.addLast(3);

        assertTrue(ad1.equals(ad2));
    }
}
