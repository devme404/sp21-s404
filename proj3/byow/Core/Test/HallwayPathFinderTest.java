package byow.Core;

import byow.TileEngine.TETile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HallwayPathFinderTest {
    private static GameMap map(int width, int height) {
        return new GameMap(new TETile[width][height]);
    }

    private static List<Point> path(Point start, Point end) {
        List<Point> path = new ArrayList<>();
        path.add(start);
        path.add(end);
        return path;
    }

    @Test
    public void findsPathAroundWall() {
        GameMap map = map(9, 7);

        // Wall at x=4, y=1..4. There is a gap at y=5.
        for (int y = 1; y <= 4; y++) {
            map.setTileType(4, y, TileType.ROOM);
        }

        List<Point> path = path(new Point(2, 3), new Point(6, 3));

        assertTrue(HallwayPathFinder.findPath(path, map));
        assertEquals(new Point(2, 3), path.get(0));
        assertEquals(new Point(6, 3), path.get(path.size() - 1));

        for (int i = 0; i < path.size(); i++) {
            Point p = path.get(i);
            assertTrue(p.x >= 0 && p.x < map.width);
            assertTrue(p.y >= 0 && p.y < map.height);
            assertNotEquals(TileType.ROOM, map.getTileType(p.x, p.y));

            if (i > 0) {
                Point prev = path.get(i - 1);
                int distance = Math.abs(prev.x - p.x) + Math.abs(prev.y - p.y);
                assertEquals("path is not contiguous at index " + i, 1, distance);
            }
        }
    }

    @Test
    public void returnsFalseWhenWallCompletelyBlocksPath() {
        GameMap map = map(9, 7);

        // Full wall at x=4 across the walkable area.
        for (int y = 1; y <= 5; y++) {
            map.setTileType(4, y, TileType.ROOM);
        }

        List<Point> path = path(new Point(2, 3), new Point(6, 3));

        assertFalse(HallwayPathFinder.findPath(path, map));
        assertEquals("failed path should not be partially rewritten", 2, path.size());
        for (int x = 0; x < map.width; x++) {
            for (int y = 0; y < map.height; y++) {
                assertNotEquals(TileType.HALLWAY, map.getTileType(x, y));
            }
        }
    }

    @Test
    public void existingHallwaysArePassable() {
        GameMap map = map(9, 7);

        for (int y = 1; y <= 3; y++) {
            map.setTileType(4, y, TileType.ROOM);
        }
        map.setTileType(4, 4, TileType.HALLWAY);

        List<Point> path = path(new Point(2, 3), new Point(6, 3));

        assertTrue(HallwayPathFinder.findPath(path, map));
    }
}
