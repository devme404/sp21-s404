package byow.Core;

import java.util.List;

public class WorldGenerator {
    private static final long ROOM_STREAM = 0x1111111111111111L;
    private static final long HALL_STREAM = 0x2222222222222222L;
    private static final long PLAYER_STREAM = 0x3333333333333333L;
    private static final int MAX_ATTEMPTS = 100;

    private int width, height;
    private long seed;

    WorldGenerator(int width, int height, long seed) {
        this.seed = seed;
        this.width = width;
        this.height = height;
    }

    public World generate() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            RandomGen roomRandom = new RandomGen(subSeed(seed, attempt, ROOM_STREAM));
            RandomGen hallwayRandom = new RandomGen(subSeed(seed, attempt, HALL_STREAM));
            RandomGen playerRandom = new RandomGen(subSeed(seed, attempt, PLAYER_STREAM));
            GameMap map = new GameMap(width, height);

            List<Room> rooms = generateRooms(map, roomRandom);
            HallwayGenerationResult result = generateHallways(map, hallwayRandom, rooms);
            if (!result.success) {
                continue;
            }
            List<Hallway> hallways = result.hallways;
            Player player = spawnPlayer(rooms, playerRandom);

            return new World(seed, map, rooms, hallways, player);
        }

        throw new WorldGenerationException("failed after " + MAX_ATTEMPTS + " attempts, seed=" + seed);
    }

    List<Room> generateRooms(GameMap map, RandomGen random) {
        return new RoomGenerator(width, height, random).generate(map);
    }

    HallwayGenerationResult generateHallways(GameMap map, RandomGen random, List<Room> rooms) {
        return new HallwayGenerator(width, height, random).generate(map, rooms);
    }

    private Player spawnPlayer(List<Room> rooms, RandomGen random) {
        Room room = rooms.get(random.intTo(rooms.size()));
        return new Player(room.centerX(), room.centerY());
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static long subSeed(long masterSeed, int attempt, long stream) {
        return mix(masterSeed
                + 0x9E3779B97F4A7C15L * (attempt + 1)
                + stream);
    }
}
