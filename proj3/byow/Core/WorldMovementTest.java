package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class WorldMovementTest {
    @Test
    public void movingPlayerRestoresOldTileAndDrawsNewAvatar() {
        GameMap map = new GameMap(5, 5);
        map.setTileType(2, 2, Tileset.FLOOR);
        map.setTileType(3, 2, Tileset.FLOOR);

        Player player = new Player(2, 2);
        World world = new World(0, map, new ArrayList<>(), new ArrayList<>(), player);

        world.move('d');

        TETile[][] tiles = world.getTiles();
        assertEquals(Tileset.FLOOR, tiles[2][2]);
        assertEquals(Tileset.AVATAR, tiles[3][2]);
        assertEquals(3, world.player().x);
        assertEquals(2, world.player().y);
    }

    @Test
    public void movingIntoWallKeepsPlayerInPlace() {
        GameMap map = new GameMap(5, 5);
        map.setTileType(2, 2, Tileset.FLOOR);

        Player player = new Player(2, 2);
        World world = new World(0, map, new ArrayList<>(), new ArrayList<>(), player);

        world.move('d');

        TETile[][] tiles = world.getTiles();
        assertEquals(Tileset.AVATAR, tiles[2][2]);
        assertEquals(Tileset.NOTHING, tiles[3][2]);
        assertEquals(2, world.player().x);
        assertEquals(2, world.player().y);
    }
}
