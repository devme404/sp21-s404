package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Drawer {
    private static final TETile ROOM_TILE = Tileset.FLOOR;
    private final GameMap map;
    private static final int[][] adj = {
            { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
    };

    Drawer(GameMap map) {
        this.map = map;
    }

    public void drawRoom(Room room) {
        for (int dx = 0; dx < room.width; dx++) {
            map.setTileType(room.left() + dx, room.bottom(), Tileset.WALL);
            map.setTileType(room.left() + dx, room.top(), Tileset.WALL);
        }
        for (int dy = 0; dy < room.height; dy++) {
            map.setTileType(room.left(), room.bottom() + dy, Tileset.WALL);
            map.setTileType(room.right(), room.bottom() + dy, Tileset.WALL);
        }

        for (int dx = 1; dx < room.width - 1; dx++) {
            for (int dy = 1; dy < room.height - 1; dy++) {
                map.setTileType(room.left() + dx, room.bottom() + dy, ROOM_TILE);
            }
        }
    }

    public void drawHallway(Hallway hallway) {
        for (Point p : hallway.points) {
            drawHallwayPoint(p);
        }
        if (hallway.width == 2) {
            for (Point p : hallway.addition) {
                drawHallwayPoint(p);
            }
        }
    }

    private void drawHallwayPoint(Point p) {
        map.setTileType(p.x, p.y, Tileset.FLOOR);
        for (int i = 0; i < adj.length; i++) {
            int nx = p.x + adj[i][0];
            int ny = p.y + adj[i][1];
            if (!map.inBound(nx, ny)) {
                continue;
            }
            TETile type = map.getTileType(nx, ny);
            map.setTileType(nx, ny,
                    (type == Tileset.NOTHING && map.getLogicalTileType(nx, ny) != TileType.HALLWAY) ? Tileset.WALL
                            : type);
        }
    }

    public void drawFloor(int x, int y) {
        map.setTileType(x, y, Tileset.FLOOR);
    }

    public void drawPlayer(Player player) {
        map.setTileType(player.x, player.y, Tileset.AVATAR);
    }
}
