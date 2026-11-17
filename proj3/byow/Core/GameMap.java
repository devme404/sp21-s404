package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class GameMap {
    public final int width, height;
    private final TETile[][] tiles;
    private final Tile[][] logical_tiles;

    GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new TETile[width][height];
        this.logical_tiles = new Tile[width][height];

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

    public void setTileType(int x, int y, TETile t) {
        tiles[x][y] = t;
    }

    public TETile getTileType(int x, int y) {
        return tiles[x][y];
    }

    public void setLogicalTileType(int x, int y, TileType type) {
        logical_tiles[x][y].set(type);
    }

    public TileType getLogicalTileType(int x, int y) {
        return logical_tiles[x][y].get();
    }

    public boolean inBound(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isWalkable(int x, int y) {
        return tiles[x][y] == Tileset.FLOOR || tiles[x][y] == Tileset.AVATAR;
    }

    public boolean isPassable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return logical_tiles[x][y].get() == TileType.EMPTY || logical_tiles[x][y].get() == TileType.HALLWAY;
    }

    public boolean positionValid(Player player) {
        if (!inBound(player.x, player.y)) {
            return false;
        }

        if (!isWalkable(player.x, player.y)) {
            return false;
        }

        return true;
    }

    public TETile[][] getTiles() {
        return tiles;
    }
}
