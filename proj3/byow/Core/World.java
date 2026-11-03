package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.List;
import java.util.ArrayList;

public class World {
    // Basic variables of the world
    private Map map;
    private Drawer drawer;
    private WorldGenerator generator;
    private List<Room> rooms;
    private List<Hallway> hallways;
    private Player player;

    World(long seed, TETile[][] tiles) {
        map = new Map(tiles);
        rooms = new ArrayList<>();
        hallways = new ArrayList<>();
        drawer = new Drawer(tiles);
        generator = new WorldGenerator(map.width, map.height, seed);

        generate();

        player = new Player(generator, map);
        drawer.drawPlayer(player);
    }


    void generate() {
        generator.generateRooms(map, rooms);
        for (Room room : rooms) {
            drawer.drawRoom(room);
        }
        generator.generateHallways(map, rooms, hallways);
    }
    
    public void move(char key) {
        player.move(key, map);
    }
}
