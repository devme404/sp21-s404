package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class WorldSizeTest {
    private static final int SEEDS = 30;

    @Parameters(name = "{0}x{1}")
    public static Collection<Object[]> sizes() {
        return Arrays.asList(new Object[][]{
                {30, 20},
                {40, 20},
                {60, 30},
                {80, 30},
                {150, 80}
        });
    }

    private final int width;
    private final int height;

    public WorldSizeTest(int width, int height) {
        this.width = width;
        this.height = height;
    }

    private static class Snapshot {
        final World world;
        final TETile[][] tiles;

        Snapshot(World world, TETile[][] tiles) {
            this.world = world;
            this.tiles = tiles;
        }
    }

    private Snapshot generate(long seed) {
        World world = new WorldGenerator(width, height, seed).generate();
        return new Snapshot(world, world.getTiles());
    }

    private static boolean isFloorLike(TETile t) {
        return t == Tileset.FLOOR || t == Tileset.AVATAR;
    }

    @Test(timeout = 30000)
    public void noNullsAndOnlyKnownTiles() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Snapshot s = generate(seed);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    TETile tile = s.tiles[x][y];
                    assertNotNull("null tile at " + x + "," + y
                            + " for seed " + seed, tile);
                    assertTrue("unexpected tile at " + x + "," + y
                                    + " for seed " + seed,
                            tile == Tileset.NOTHING
                                    || isFloorLike(tile)
                                    || tile == Tileset.WALL);
                }
            }
        }
    }

    @Test(timeout = 30000)
    public void allFloorsAreConnected() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Snapshot s = generate(seed);
            int[] start = firstFloor(s.tiles);
            assertNotNull("no floor for seed " + seed, start);

            int total = countFloor(s.tiles);
            int reachable = floodFill(s.tiles, start[0], start[1]);
            assertEquals("isolated floor for size " + width + "x" + height
                            + " seed " + seed + "\n" + ascii(s.tiles),
                    total, reachable);
        }
    }

    @Test(timeout = 30000)
    public void playerSpawnedOnAvatarTile() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Snapshot s = generate(seed);
            Player player = s.world.player();

            assertNotNull("player is null for seed " + seed, player);
            assertTrue(player.x >= 0 && player.x < width);
            assertTrue(player.y >= 0 && player.y < height);
            assertEquals("player is not drawn as AVATAR for seed " + seed,
                    Tileset.AVATAR, s.tiles[player.x][player.y]);
            assertTrue("player spawned on a wall", isFloorLike(
                    s.tiles[player.x][player.y]));
        }
    }

    @Test(timeout = 30000)
    public void roomsRespectBoundsAndKeepAGap() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Snapshot s = generate(seed);
            List<Room> rooms = s.world.rooms();

            assertFalse("no rooms for seed " + seed, rooms.isEmpty());

            for (Room room : rooms) {
                assertTrue(room.width >= 3);
                assertTrue(room.height >= 3);
                assertTrue(room.left() >= 1);
                assertTrue(room.bottom() >= 1);
                assertTrue(room.right() < width - 1);
                assertTrue(room.top() < height - 1);
            }

            for (int i = 0; i < rooms.size(); i++) {
                for (int j = i + 1; j < rooms.size(); j++) {
                    Room a = rooms.get(i);
                    Room b = rooms.get(j);

                    boolean separatedOnX = a.right() + 1 < b.left()
                            || b.right() + 1 < a.left();
                    boolean separatedOnY = a.top() + 1 < b.bottom()
                            || b.top() + 1 < a.bottom();

                    assertTrue("rooms touch for seed " + seed + ": "
                                    + "room " + i + " and room " + j,
                            separatedOnX || separatedOnY);
                }
            }
        }
    }

    @Test(timeout = 30000)
    public void hallwaysAreInBoundsAndHaveValidWidth() {
        boolean sawWidthTwo = false;

        for (long seed = 0; seed < SEEDS; seed++) {
            Snapshot s = generate(seed);
            List<Hallway> hallways = s.world.hallways();
            assertFalse("no hallways for seed " + seed, hallways.isEmpty());

            for (Hallway hallway : hallways) {
                assertTrue(hallway.width == 1 || hallway.width == 2);
                assertFalse(hallway.points.isEmpty());

                if (hallway.width == 2) {
                    sawWidthTwo = true;
                    assertNotNull("width-2 hallway has no additions", hallway.addition);
                    assertFalse("width-2 hallway has empty additions", hallway.addition.isEmpty());
                    for (Point p : hallway.addition) {
                        assertTrue(p.x >= 0 && p.x < width);
                        assertTrue(p.y >= 0 && p.y < height);
                        assertTrue(isAdjacentToAny(p, hallway.points));
                    }
                }

                for (Point p : hallway.points) {
                    assertTrue(p.x >= 0 && p.x < width);
                    assertTrue(p.y >= 0 && p.y < height);
                    assertTrue("hallway point is not floor-like at "
                                    + p.x + "," + p.y,
                            isFloorLike(s.tiles[p.x][p.y]));
                }
            }
        }

        assertTrue("no width-2 hallway generated for size "
                + width + "x" + height, sawWidthTwo);
    }

    @Test(timeout = 30000)
    public void sameSeedIsReproducible() {
        Snapshot a = generate(12);
        Snapshot b = generate(12);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                assertEquals("tile at " + x + "," + y,
                        a.tiles[x][y].character(),
                        b.tiles[x][y].character());
            }
        }
    }

    private static boolean isAdjacentToAny(Point point, List<Point> points) {
        for (Point p : points) {
            if (Math.abs(point.x - p.x) + Math.abs(point.y - p.y) == 1) {
                return true;
            }
        }
        return false;
    }

    private static int[] firstFloor(TETile[][] t) {
        for (int x = 0; x < t.length; x++) {
            for (int y = 0; y < t[0].length; y++) {
                if (isFloorLike(t[x][y])) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static int countFloor(TETile[][] t) {
        int n = 0;
        for (int x = 0; x < t.length; x++) {
            for (int y = 0; y < t[0].length; y++) {
                if (isFloorLike(t[x][y])) {
                    n++;
                }
            }
        }
        return n;
    }

    private static int floodFill(TETile[][] t, int sx, int sy) {
        boolean[][] seen = new boolean[t.length][t[0].length];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{sx, sy});
        seen[sx][sy] = true;
        int count = 0;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!q.isEmpty()) {
            int[] p = q.remove();
            count++;
            for (int[] d : dirs) {
                int nx = p[0] + d[0];
                int ny = p[1] + d[1];
                if (nx >= 0 && nx < t.length && ny >= 0 && ny < t[0].length
                        && !seen[nx][ny] && isFloorLike(t[nx][ny])) {
                    seen[nx][ny] = true;
                    q.add(new int[]{nx, ny});
                }
            }
        }
        return count;
    }

    private static String ascii(TETile[][] t) {
        StringBuilder sb = new StringBuilder();
        for (int y = t[0].length - 1; y >= 0; y--) {
            for (int x = 0; x < t.length; x++) {
                sb.append(t[x][y].character());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
