package byow.Core;

import java.util.List;

import byow.TileEngine.TETile;

public class World {

    // Basic variables of the world
    private long seed;
    private GameMap map;
    private Drawer drawer;
    private List<Room> rooms;
    private List<Hallway> hallways;
    private Player player;

    World(long seed, GameMap map, List<Room> rooms, List<Hallway> hallways, Player player) {
        this.seed = seed;
        this.map = map;
        this.rooms = rooms;
        this.hallways = hallways;
        this.player = player;
        drawer = new Drawer(map);
        draw(rooms, hallways, player);
    }

    void draw(List<Room> rooms, List<Hallway> hallways, Player player) {
        for (Room room : rooms) {
            drawer.drawRoom(room);
        }
        for (Hallway hallway : hallways) {
            drawer.drawHallway(hallway);
        }
        drawer.drawPlayer(player);
    }

    public void move(char key) {
        Player new_player = new Player(player);
        switch (key) {
            case 'w':
                new_player = new_player.moved(0, 1);
                break;
            case 's':
                new_player = new_player.moved(0, -1);
                break;
            case 'a':
                new_player = new_player.moved(-1, 0);
                break;
            case 'd':
                new_player = new_player.moved(1, 0);
                break;
            default:
                break;
        }

        if (map.positionValid(new_player)) {
            drawer.drawFloor(player.x, player.y);
            player = new_player;
            drawer.drawPlayer(player);
        }
    }

    List<Room> rooms() {
        return rooms;
    }

    List<Hallway> hallways() {
        return hallways;
    }

    Player player() {
        return player;
    }

    TETile[][] getTiles() {
        return map.getTiles();
    }
}
