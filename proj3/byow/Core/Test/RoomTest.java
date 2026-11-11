package byow.Core;

import org.junit.Test;

import static org.junit.Assert.*;

public class RoomTest {
    @Test
    public void centerAndBoundsAreInclusive() {
        Room room = new Room(0, 4, 3, 2, 5);

        assertEquals(2, room.left());
        assertEquals(5, room.bottom());
        assertEquals(5, room.right());
        assertEquals(7, room.top());
        assertEquals(3, room.centerX());
        assertEquals(6, room.centerY());
    }

    @Test
    public void sideTowardUsesDominantAxis() {
        Room origin = new Room(0, 4, 4, 0, 0);
        Room right = new Room(1, 4, 4, 10, 0);
        Room left = new Room(2, 4, 4, -10, 0);
        Room above = new Room(3, 4, 4, 0, 10);
        Room below = new Room(4, 4, 4, 0, -10);

        assertEquals(Side.RIGHT, origin.sideToward(right));
        assertEquals(Side.LEFT, right.sideToward(origin));
        assertEquals(Side.LEFT, origin.sideToward(left));
        assertEquals(Side.TOP, origin.sideToward(above));
        assertEquals(Side.BOTTOM, origin.sideToward(below));
    }

    @Test
    public void overlapIncludesSharedBoundaryCells() {
        Room a = new Room(0, 4, 4, 0, 0);   // x: 0..3, y: 0..3
        Room sharingCell = new Room(1, 4, 4, 3, 0); // shares x=3
        Room adjacent = new Room(2, 4, 4, 4, 0);    // no shared cell
        Room disjoint = new Room(3, 4, 4, 10, 10);

        assertTrue(a.overlap(sharingCell));
        assertFalse(a.overlap(adjacent));
        assertFalse(a.overlap(disjoint));
    }
}
