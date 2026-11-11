package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.List;
import java.util.ArrayList;

public class World {
    private static final int MAX_TRIES = 100;

    // Basic variables of the world
    private GameMap map;
    private Drawer drawer;
    private WorldGenerator generator;
    private List<Room> rooms;
    private List<Hallway> hallways;
    private Player player;

    World(long seed, TETile[][] tiles) {
        map = new GameMap(tiles);
        rooms = new ArrayList<>();
        hallways = new ArrayList<>();
        drawer = new Drawer(tiles);
        generator = new WorldGenerator(map.width, map.height, seed);

        generate();

        player = spawnPlayer(generator, map);
        drawer.drawPlayer(player);
    }


    void generate() {
        generator.generateRooms(map, rooms);
        for (Room room : rooms) {
            drawer.drawRoom(room);
        }
        generator.generateHallways(map, rooms, hallways);
        for (Hallway hallway : hallways) {
            drawer.drawHallway(hallway);
        }
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

        if (positionValid(new_player, map)) {
            this.player = new_player;
        }
    }

    private Player spawnPlayer(WorldGenerator generator, GameMap map) {
        int tries = 0;
        while (tries < MAX_TRIES) {
            Player new_player = generator.intializePlayer();
            tries++;
            if (positionValid(new_player, map)) {
                return new_player;
            }
        }
        return null;
    }

    private boolean positionValid(Player player, GameMap map) {
        if (player.x < 0 || player.x >= map.width || player.y < 0 || player.y >= map.height) {
            return false;
        }

        if (!map.isWalkable(player.x, player.y)) {
            return false;
        }

        return true;
    }
}
