package byow.Core;

import java.util.List;

public class WorldGenerator {
    // Magic numbers to decide some maximum values for rooms and hallways
    // Divide the minimum one between the width and the height by these numbers to get corresponding values

    private int width, height;
    private long seed;
    private RandomGen random;
    private RoomGenerator room_generator;
    private HallwayGenerator hallway_generator;

    WorldGenerator(int width, int height, long seed) {
        this.seed = seed;
        this.width = width;
        this.height = height;

        random = new RandomGen(seed);
        room_generator = new RoomGenerator(width, height, random);
        hallway_generator = new HallwayGenerator(width, height, random);
    }

    void generateRooms(Map map, List<Room> rooms) {
        room_generator.generate(map, rooms);
    }

    void generatorHallways(Map map, List<Room> rooms, List<Hallway> hallways) {
        hallway_generator.generate(map, rooms, hallways);
    }

    public Player intializePlayer() {
        Player result = new Player(random.intTo(width), random.intTo(height));
        return result;
    }
}
