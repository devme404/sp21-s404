package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.Test;

import java.util.ArrayDeque;

import static org.junit.Assert.*;

public class WorldTest {
    private static final int W = 80;
    private static final int H = 30;

    private static TETile[][] generate(long seed) {
        TETile[][] tiles = new TETile[W][H];
        new World(seed, tiles);
        return tiles;
    }

    @Test(timeout = 5000)
    public void tilesAreNotNullAndInPalette() {
        TETile[][] t = generate(12);
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                assertNotNull("null at " + x + "," + y, t[x][y]);
                assertTrue("unexpected tile at " + x + "," + y,
                    t[x][y] == Tileset.NOTHING || t[x][y] == Tileset.FLOOR || t[x][y] == Tileset.WALL);
            }
        }
    }

   @Test(timeout = 5000)
   public void sameSeedIsReproducible() {
       TETile[][] a = generate(12);
       TETile[][] b = generate(12);
       for (int x = 0; x < W; x++) {
           for (int y = 0; y < H; y++) {
               assertEquals("tile at " + x + "," + y,
                       a[x][y].character(), b[x][y].character());
           }
       }
   }

   @Test(timeout = 5000)
   public void allFloorsAreConnected() {
       TETile[][] t = generate(12);
       int[] start = firstFloor(t);
       assertNotNull("world has no floor", start);

       int total = countFloor(t);
       int reachable = floodFill(t, start[0], start[1]);
       assertEquals("isolated floor\n" + ascii(t), total, reachable);
   }

   private static int[] firstFloor(TETile[][] t) {
       for (int x = 0; x < t.length; x++) {
           for (int y = 0; y < t[0].length; y++) {
               if (t[x][y] == Tileset.FLOOR) {
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
               if (t[x][y] == Tileset.FLOOR) {
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
                       && !seen[nx][ny] && t[nx][ny] == Tileset.FLOOR) {
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
