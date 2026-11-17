package byow.Core;

import org.junit.Test;

import static org.junit.Assert.*;

public class PointTest {
    @Test
    public void equalsUsesCoordinates() {
        Point a = new Point(3, 7);
        Point b = new Point(3, 7);
        Point c = new Point(7, 3);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "3,7");
    }
}
