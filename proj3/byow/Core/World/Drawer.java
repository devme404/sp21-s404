package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Drawer {
    private static final TETile ROOM_TILE = Tileset.SAND;
    private TETile[][] tiles;
    private static final int[][] adj = {
        {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

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
                tiles[room.left() + dx][room.bottom() + dy] = ROOM_TILE;
            }
        }
    }

    public void drawHallway(Hallway hallway) {
        for (Point p : hallway.points) {
            tiles[p.x][p.y] = Tileset.FLOOR;
            for (int i = 0 ; i < adj.length; i++) {
                int nx = p.x + adj[i][0];
                int ny = p.y + adj[i][1];
                TETile type = tiles[nx][ny];
                tiles[nx][ny] = (type == Tileset.NOTHING) ? Tileset.WALL : type;
            }
        }
    }


    public void drawPlayer(Player player) {
        tiles[player.x][player.y] = Tileset.AVATAR;
    }
}
