package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Drawer {
    private TETile[][] tiles;

    Drawer(TETile[][] tiles) {
        this.tiles = tiles;
    }

    public void drawRoom(Room room) {
        for (int dx = 0; dx < room.width; dx++) {
            tiles[room.left() + dx][room.bottom()] = Tileset.WALL;
            tiles[room.left() + dx][room.top()] = Tileset.WALL;
        }
        for (int dy = 0; dy < room.height; dy++) {
            tiles[room.left()][room.bottom() + dy] = Tileset.WALL;
            tiles[room.right()][room.bottom() + dy] = Tileset.WALL;
        }

        for (int dx = 1; dx < room.width - 1; dx++) {
            for (int dy = 1; dy < room.height - 1; dy++) {
                tiles[room.left() + dx][room.bottom() + dy] = Tileset.FLOOR;
            }
        }
    }


    public void drawPlayer(Player player) {
        tiles[player.x][player.y] = Tileset.AVATAR;
    }
}
