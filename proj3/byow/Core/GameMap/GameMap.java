package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class GameMap {
    public final int width, height;
    private TETile[][] tiles;
    private Tile[][] logical_tiles;

    GameMap(TETile[][] tiles) {
        width = tiles.length;
        height = tiles[0].length;

        this.tiles = tiles;
        logical_tiles = new Tile[width][height];

        initialize();
    }

    private void initialize() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = Tileset.NOTHING;
                logical_tiles[x][y] = new Tile(TileType.EMPTY);
            }
        }
    }

    public void setTileType(int x, int y, TileType type) {
        logical_tiles[x][y].set(type);
    }

    public TileType getTileType(int x, int y) {
        return logical_tiles[x][y].get();
    }

    public boolean isWalkable(int x, int y) {
        return tiles[x][y] == Tileset.FLOOR || tiles[x][y] == Tileset.AVATAR;
    }
}
