package byow.Core;

import org.junit.Test;
import static org.junit.Assert.*;

public class DSUTest {
    @Test
    public void newDSUStartsDisconnected() {
        DSU dsu = new DSU(4);
        
        assertFalse(dsu.isConnected(0, 1));
        assertTrue(dsu.union(0, 1));

        assertFalse(dsu.isConnected(0, 2));
        assertFalse(dsu.isConnected(1, 2));
        assertTrue(dsu.union(1, 2));
        assertTrue(dsu.isConnected(0, 2));
        assertTrue(dsu.isConnected(1, 2));
    }

    @Test
    public void DSUUnionSimple() {
        int size = 1000;
        DSU dsu = new DSU(size);

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                assertFalse(dsu.isConnected(i, j));
            }
        }

        for (int i = 0; i < size - 1; i++) {
            assertTrue(dsu.union(i, i + 1));
            for (int j = 0; j < i + 1; j++) {
                assertTrue(dsu.isConnected(j, i + 1));
            }
        }
    }

    @Test
    public void sizeTracksComponentSize() {
        DSU dsu = new DSU(4);

        dsu.union(0, 1);
        dsu.union(1, 2);

        assertEquals(3, dsu.size(0));
        assertEquals(3, dsu.size(2));
        assertEquals(1, dsu.size(3));
    }

}
