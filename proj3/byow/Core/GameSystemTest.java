package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameSystemTest {
    private static final int W = 80;
    private static final int H = 30;

    @Test(timeout = 20000)
    public void newCommandParsesSeedAndCreatesWorld() {
        GameSystem system = new GameSystem(W, H);
        system.runInput("n123");

        TETile[][] tiles = system.getTiles();
        assertNotNull(tiles);
        assertEquals(W, tiles.length);
        assertEquals(H, tiles[0].length);

        int avatars = 0;
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                assertNotNull(tiles[x][y]);
                if (tiles[x][y] == Tileset.AVATAR) {
                    avatars++;
                }
            }
        }
        assertEquals(1, avatars);
    }

    @Test(timeout = 20000)
    public void sameInputIsReproducible() {
        GameSystem a = new GameSystem(W, H);
        GameSystem b = new GameSystem(W, H);

        a.runInput("n123");
        b.runInput("n123");

        TETile[][] first = a.getTiles();
        TETile[][] second = b.getTiles();
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                assertEquals("tile at " + x + "," + y,
                        first[x][y].character(), second[x][y].character());
            }
        }
    }

    @Test(timeout = 20000)
    public void movementAfterNewCommandDoesNotCrash() {
        GameSystem system = new GameSystem(W, H);
        system.runInput("n123s");

        TETile[][] tiles = system.getTiles();
        assertNotNull(tiles);

        int avatars = 0;
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                if (tiles[x][y] == Tileset.AVATAR) {
                    avatars++;
                }
            }
        }
        assertEquals(1, avatars);
    }

    @Test
    public void missingSeedIsRejected() {
        GameSystem system = new GameSystem(W, H);
        try {
            system.runInput("n");
            fail("expected missing seed to be rejected");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}
