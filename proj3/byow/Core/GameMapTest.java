package byow.Core;

import byow.TileEngine.Tileset;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameMapTest {
    @Test
    public void newMapHasCompleteTileAndLogicalArrays() {
        GameMap map = new GameMap(4, 3);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 3; y++) {
                assertNotNull(map.getTileType(x, y));
                assertEquals(Tileset.NOTHING, map.getTileType(x, y));
                assertEquals(TileType.EMPTY, map.getLogicalTileType(x, y));
            }
        }
    }

    @Test
    public void positionValidChecksBoundsAndWalkability() {
        GameMap map = new GameMap(5, 4);
        map.setTileType(2, 2, Tileset.FLOOR);

        assertTrue(map.positionValid(new Player(2, 2)));
        assertFalse(map.positionValid(new Player(-1, 2)));
        assertFalse(map.positionValid(new Player(5, 2)));
        assertFalse(map.positionValid(new Player(2, -1)));
        assertFalse(map.positionValid(new Player(2, 4)));
        assertFalse(map.positionValid(new Player(1, 2)));
    }
}
