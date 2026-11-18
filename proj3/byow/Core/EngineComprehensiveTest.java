package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * End-to-end tests that go through Engine, which is the entry point used by
 * the autograder.
 */
public class EngineComprehensiveTest {
    private static final int SEEDS = 20;

    private static final int[] DX = {0, 0, -1, 1};
    private static final int[] DY = {1, -1, 0, 0};
    private static final char[] MOVES = {'w', 's', 'a', 'd'};

    private static TETile[][] run(String input) {
        return new Engine().interactWithInputString(input);
    }

    private static World world(String input) {
        Engine engine = new Engine();
        engine.interactWithInputString(input);
        return engine.system().world();
    }

    private static boolean isFloorLike(TETile tile) {
        return tile == Tileset.FLOOR || tile == Tileset.AVATAR;
    }

    private static int[] findAvatar(TETile[][] tiles) {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                if (tiles[x][y] == Tileset.AVATAR) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static int countAvatar(TETile[][] tiles) {
        int count = 0;
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                if (tiles[x][y] == Tileset.AVATAR) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countFloorLike(TETile[][] tiles) {
        int count = 0;
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                if (isFloorLike(tiles[x][y])) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int floodFill(TETile[][] tiles, int sx, int sy) {
        boolean[][] seen = new boolean[tiles.length][tiles[0].length];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{sx, sy});
        seen[sx][sy] = true;
        int count = 0;

        while (!queue.isEmpty()) {
            int[] p = queue.remove();
            count++;
            for (int i = 0; i < DX.length; i++) {
                int nx = p[0] + DX[i];
                int ny = p[1] + DY[i];
                if (nx >= 0 && nx < tiles.length
                        && ny >= 0 && ny < tiles[0].length
                        && !seen[nx][ny]
                        && isFloorLike(tiles[nx][ny])) {
                    seen[nx][ny] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }
        return count;
    }

    private static void assertNotEqual(TETile[][] first, TETile[][] second) {
        assertEquals(first.length, second.length);
        assertEquals(first[0].length, second[0].length);
        for (int x = 0; x < first.length; x++) {
            for (int y = 0; y < first[0].length; y++) {
                if (first[x][y].character() != second[x][y].character()) {
                    return;
                }
            }
        }
        fail("expected different worlds for different seeds");
    }

    private static void assertFramesEqual(TETile[][] expected, TETile[][] actual) {
        assertEquals(expected.length, actual.length);
        assertEquals(expected[0].length, actual[0].length);
        for (int x = 0; x < expected.length; x++) {
            for (int y = 0; y < expected[0].length; y++) {
                assertEquals("tile at " + x + "," + y,
                        expected[x][y].character(), actual[x][y].character());
            }
        }
    }

    @Test(timeout = 60000)
    public void allSeedsProduceValidConnectedWorlds() {
        boolean sawWidthTwo = false;
        boolean sawHallwayTurn = false;
        boolean sawWall = false;
        boolean sawFloor = false;
        boolean sawNothing = false;

        Set<Integer> roomCounts = new HashSet<>();
        Set<Integer> hallwayCounts = new HashSet<>();
        Set<Integer> roomWidths = new HashSet<>();
        Set<Integer> roomHeights = new HashSet<>();
        Set<Integer> roomPositions = new HashSet<>();
        Set<Integer> hallwayLengths = new HashSet<>();
        Set<Integer> hallwayWidths = new HashSet<>();

        for (long seed = 0; seed < SEEDS; seed++) {
            Engine engine = new Engine();
            TETile[][] tiles = engine.interactWithInputString("N" + seed + "S");
            World world = engine.system().world();

            assertNotNull("null world for seed " + seed, world);
            assertEquals(Engine.WIDTH, tiles.length);
            assertEquals(Engine.HEIGHT, tiles[0].length);

            for (int x = 0; x < tiles.length; x++) {
                for (int y = 0; y < tiles[0].length; y++) {
                    assertNotNull("null tile at " + x + "," + y
                            + " for seed " + seed, tiles[x][y]);
                    assertTrue("unexpected tile at " + x + "," + y
                                    + " for seed " + seed,
                            tiles[x][y] == Tileset.NOTHING
                                    || isFloorLike(tiles[x][y])
                                    || tiles[x][y] == Tileset.WALL);
                    if (tiles[x][y] == Tileset.WALL) {
                        sawWall = true;
                    } else if (isFloorLike(tiles[x][y])) {
                        sawFloor = true;
                    } else if (tiles[x][y] == Tileset.NOTHING) {
                        sawNothing = true;
                    }
                }
            }

            assertEquals("avatar count for seed " + seed,
                    1, countAvatar(tiles));

            Player player = world.player();
            assertNotNull("null player for seed " + seed, player);
            assertEquals(Tileset.AVATAR, tiles[player.x][player.y]);

            int[] avatar = findAvatar(tiles);
            assertNotNull(avatar);
            assertEquals(player.x, avatar[0]);
            assertEquals(player.y, avatar[1]);

            assertEquals("floor components for seed " + seed,
                    countFloorLike(tiles),
                    floodFill(tiles, avatar[0], avatar[1]));

            List<Room> rooms = world.rooms();
            List<Hallway> hallways = world.hallways();
            assertFalse("no rooms for seed " + seed, rooms.isEmpty());
            assertFalse("no hallways for seed " + seed, hallways.isEmpty());

            roomCounts.add(rooms.size());
            hallwayCounts.add(hallways.size());

            for (Room room : rooms) {
                roomWidths.add(room.width);
                roomHeights.add(room.height);
                roomPositions.add(room.left() * 1000 + room.bottom());
                assertTrue(room.left() >= 1);
                assertTrue(room.bottom() >= 1);
                assertTrue(room.right() < Engine.WIDTH - 1);
                assertTrue(room.top() < Engine.HEIGHT - 1);
            }

            for (int i = 0; i < rooms.size(); i++) {
                for (int j = i + 1; j < rooms.size(); j++) {
                    Room a = rooms.get(i);
                    Room b = rooms.get(j);
                    boolean separatedOnX = a.right() + 1 < b.left()
                            || b.right() + 1 < a.left();
                    boolean separatedOnY = a.top() + 1 < b.bottom()
                            || b.top() + 1 < a.bottom();
                    assertTrue("rooms touch for seed " + seed,
                            separatedOnX || separatedOnY);
                }
            }

            for (Hallway hallway : hallways) {
                assertTrue(hallway.width == 1 || hallway.width == 2);
                assertFalse(hallway.points.isEmpty());
                hallwayWidths.add(hallway.width);
                hallwayLengths.add(hallway.points.size());

                if (hallway.width == 2) {
                    sawWidthTwo = true;
                    assertNotNull(hallway.addition);
                    assertFalse(hallway.addition.isEmpty());
                }

                for (Point p : hallway.points) {
                    assertTrue(p.x >= 0 && p.x < Engine.WIDTH);
                    assertTrue(p.y >= 0 && p.y < Engine.HEIGHT);
                }
                if (hallway.addition != null) {
                    for (Point p : hallway.addition) {
                        assertTrue(p.x >= 0 && p.x < Engine.WIDTH);
                        assertTrue(p.y >= 0 && p.y < Engine.HEIGHT);
                    }
                }

                for (int i = 1; i < hallway.points.size() - 1; i++) {
                    Point before = hallway.points.get(i - 1);
                    Point current = hallway.points.get(i);
                    Point after = hallway.points.get(i + 1);
                    int dx1 = current.x - before.x;
                    int dy1 = current.y - before.y;
                    int dx2 = after.x - current.x;
                    int dy2 = after.y - current.y;
                    if (dx1 != dx2 || dy1 != dy2) {
                        sawHallwayTurn = true;
                        break;
                    }
                }
            }
        }

        assertTrue("no width-2 hallway found across seeds", sawWidthTwo);
        assertTrue("no hallway with a turn found across seeds", sawHallwayTurn);
        assertTrue("no wall tiles generated", sawWall);
        assertTrue("no floor tiles generated", sawFloor);
        assertTrue("no unused tiles generated", sawNothing);

        assertTrue("room count is not random", roomCounts.size() > 1);
        assertTrue("hallway count is not random", hallwayCounts.size() > 1);
        assertTrue("room widths are not random", roomWidths.size() > 1);
        assertTrue("room heights are not random", roomHeights.size() > 1);
        assertTrue("room positions are not random", roomPositions.size() > 1);
        assertTrue("hallway lengths are not random", hallwayLengths.size() > 1);
        assertTrue("hallway widths are not random", hallwayWidths.size() > 1);
    }

    @Test(timeout = 60000)
    public void inputIsCaseInsensitiveAndSeedTerminatorIsNotMovement() {
        assertFramesEqual(run("N123S"), run("n123s"));
        assertFramesEqual(run("N123S"), run("N123"));
        assertFramesEqual(run("N123Sww"), run("n123Sww"));
        assertFramesEqual(run("N123Sww"), run("N123Sww"));
    }

    @Test(timeout = 60000)
    public void differentSeedsProduceDifferentWorlds() {
        assertNotEqual(run("N0S"), run("N1S"));
        assertNotEqual(run("N123S"), run("N124S"));
        assertNotEqual(run("N999S"), run("N1000S"));
    }

    @Test(timeout = 60000)
    public void seedParsingAndReproducibility() {
        TETile[][] n123a = run("N123S");
        TETile[][] n123b = run("N123S");
        TETile[][] n000123 = run("N000123S");

        assertFramesEqual(n123a, n123b);
        assertFramesEqual(n123a, n000123);

        TETile[][] n0 = run("N0S");
        TETile[][] n0000 = run("N0000S");
        assertFramesEqual(n0, n0000);

        assertFramesEqual(run("N123Sss"), run("N123Sss"));

        TETile[][] largeSeed = run("N9223372036854775807S");
        assertNotNull(largeSeed);
    }

    @Test(timeout = 60000)
    public void unknownCommandIsRejectedThroughEngine() {
        try {
            run("x");
            fail("expected unknown command to be rejected");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test(timeout = 60000)
    public void missingSeedIsRejectedThroughEngine() {
        Engine engine = new Engine();
        try {
            engine.interactWithInputString("N");
            fail("expected missing seed to be rejected");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test(timeout = 60000)
    public void singleStepMovementMatchesNeighboringTile() {
        long seed = -1;
        int[] start = null;
        TETile[][] base = null;

        for (long candidate = 0; candidate < 50; candidate++) {
            TETile[][] tiles = run("N" + candidate + "S");
            int[] avatar = findAvatar(tiles);
            assertNotNull(avatar);
            if (hasFloorNeighbor(tiles, avatar[0], avatar[1])) {
                seed = candidate;
                start = avatar;
                base = tiles;
                break;
            }
        }

        assertNotNull("could not find a seed with a floor neighbor", base);

        for (int direction = 0; direction < MOVES.length; direction++) {
            int tx = start[0] + DX[direction];
            int ty = start[1] + DY[direction];
            boolean inBounds = tx >= 0 && tx < base.length
                    && ty >= 0 && ty < base[0].length;
            boolean targetIsFloor = inBounds && base[tx][ty] == Tileset.FLOOR;

            TETile[][] moved = run("N" + seed + "S" + MOVES[direction]);
            int[] after = findAvatar(moved);
            assertNotNull(after);
            assertEquals(1, countAvatar(moved));

            if (targetIsFloor) {
                assertEquals("failed to move " + MOVES[direction],
                        tx, after[0]);
                assertEquals("failed to move " + MOVES[direction],
                        ty, after[1]);
                assertEquals(Tileset.FLOOR, moved[start[0]][start[1]]);
            } else {
                assertEquals("moved into non-floor " + MOVES[direction],
                        start[0], after[0]);
                assertEquals("moved into non-floor " + MOVES[direction],
                        start[1], after[1]);
                assertEquals(Tileset.AVATAR, moved[start[0]][start[1]]);
            }
        }
    }

    @Test(timeout = 60000)
    public void multiStepMovementFollowsFloorPath() {
        long seed = -1;
        List<Character> moves = null;
        int[] target = null;
        int[] start = null;
        TETile[][] base = null;

        for (long candidate = 0; candidate < 50; candidate++) {
            TETile[][] tiles = run("N" + candidate + "S");
            int[] avatar = findAvatar(tiles);
            assertNotNull(avatar);

            PathResult result = findPathAtLeast(tiles, avatar[0], avatar[1], 4);
            if (result != null) {
                seed = candidate;
                moves = result.moves;
                target = result.target;
                start = avatar;
                base = tiles;
                break;
            }
        }

        assertNotNull("could not find a floor path of length 4", base);

        StringBuilder input = new StringBuilder("N").append(seed).append("S");
        for (char move : moves) {
            input.append(move);
        }

        TETile[][] moved = run(input.toString());
        int[] after = findAvatar(moved);
        assertNotNull(after);
        assertEquals(target[0], after[0]);
        assertEquals(target[1], after[1]);
        assertEquals(1, countAvatar(moved));
        assertEquals(Tileset.FLOOR, moved[start[0]][start[1]]);
        assertEquals(Tileset.AVATAR, moved[target[0]][target[1]]);
    }

    private static boolean hasFloorNeighbor(TETile[][] tiles, int x, int y) {
        for (int i = 0; i < DX.length; i++) {
            int nx = x + DX[i];
            int ny = y + DY[i];
            if (nx >= 0 && nx < tiles.length
                    && ny >= 0 && ny < tiles[0].length
                    && tiles[nx][ny] == Tileset.FLOOR) {
                return true;
            }
        }
        return false;
    }

    private static class PathResult {
        final List<Character> moves;
        final int[] target;

        PathResult(List<Character> moves, int[] target) {
            this.moves = moves;
            this.target = target;
        }
    }

    private static PathResult findPathAtLeast(TETile[][] tiles, int sx, int sy,
                                              int minimumLength) {
        Map<Integer, Integer> parent = new HashMap<>();
        Map<Integer, Character> moveFromParent = new HashMap<>();
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{sx, sy});
        parent.put(key(sx, sy, tiles[0].length), -1);

        while (!queue.isEmpty()) {
            int[] p = queue.remove();
            int distance = distanceFromStart(parent, key(p[0], p[1], tiles[0].length));

            if (distance >= minimumLength) {
                List<Character> moves = new ArrayList<>();
                int current = key(p[0], p[1], tiles[0].length);
                while (parent.get(current) != -1) {
                    moves.add(0, moveFromParent.get(current));
                    current = parent.get(current);
                }
                return new PathResult(moves, new int[]{p[0], p[1]});
            }

            for (int i = 0; i < DX.length; i++) {
                int nx = p[0] + DX[i];
                int ny = p[1] + DY[i];
                if (nx >= 0 && nx < tiles.length
                        && ny >= 0 && ny < tiles[0].length
                        && tiles[nx][ny] == Tileset.FLOOR) {
                    int key = key(nx, ny, tiles[0].length);
                    if (!parent.containsKey(key)) {
                        parent.put(key, key(p[0], p[1], tiles[0].length));
                        moveFromParent.put(key, MOVES[i]);
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }

        return null;
    }

    private static int key(int x, int y, int height) {
        return x * height + y;
    }

    private static int distanceFromStart(Map<Integer, Integer> parent, int key) {
        int distance = 0;
        while (parent.get(key) != -1) {
            key = parent.get(key);
            distance++;
        }
        return distance;
    }
}
