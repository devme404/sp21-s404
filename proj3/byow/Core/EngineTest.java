package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.Test;

import static org.junit.Assert.*;

public class EngineTest {
    private static int avatarCount(TETile[][] tiles) {
        int count = 0;
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                assertNotNull("null tile at " + x + "," + y, tiles[x][y]);
                if (tiles[x][y] == Tileset.AVATAR) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test(timeout = 30000)
    public void engineRunsNewCommandEndToEnd() {
        Engine engine = new Engine();
        TETile[][] tiles = engine.interactWithInputString("N123S");

        assertNotNull(tiles);
        assertEquals(Engine.WIDTH, tiles.length);
        assertEquals(Engine.HEIGHT, tiles[0].length);
        assertEquals(1, avatarCount(tiles));
    }

    @Test(timeout = 30000)
    public void engineInputIsReproducible() {
        Engine first = new Engine();
        Engine second = new Engine();

        TETile[][] a = first.interactWithInputString("N123S");
        TETile[][] b = second.interactWithInputString("N123S");

        for (int x = 0; x < Engine.WIDTH; x++) {
            for (int y = 0; y < Engine.HEIGHT; y++) {
                assertEquals("tile at " + x + "," + y,
                        a[x][y].character(), b[x][y].character());
            }
        }
    }

    @Test(timeout = 30000)
    public void engineHandlesMovementInput() {
        Engine engine = new Engine();
        TETile[][] tiles = engine.interactWithInputString("N123Ss");

        assertNotNull(tiles);
        assertEquals(1, avatarCount(tiles));
    }
}
