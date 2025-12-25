package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testNoChange() {
        IntList lst = IntList.of(4, 6, 8, 10, 20);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 6 -> 8 -> 10 -> 20", lst.toString());
        assertFalse(changed);
    }

    @Test
    public void testMultipleChange() {
        IntList lst = IntList.of(1, 2, 3, 5, 7);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("1 -> 4 -> 9 -> 25 -> 49", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testNull() {
        IntList lst = null;
        boolean changed = IntListExercises.squarePrimes(lst);
        assertNull(lst);
        assertFalse(changed);
    }
}
