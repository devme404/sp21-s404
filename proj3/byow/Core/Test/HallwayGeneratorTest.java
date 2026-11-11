package byow.Core;

import byow.TileEngine.TETile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HallwayGeneratorTest {
    private static final int WIDTH = 40;
    private static final int HEIGHT = 20;

    private static GameMap emptyMap() {
        return new GameMap(new TETile[WIDTH][HEIGHT]);
    }

    private static void markRoom(GameMap map, Room room) {
        for (int x = room.left(); x <= room.right(); x++) {
            for (int y = room.bottom(); y <= room.top(); y++) {
                map.setTileType(x, y, TileType.ROOM);
            }
        }
    }

    private static int roomAtDoor(Point p, List<Room> rooms) {
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            boolean onVerticalWall = (p.x == r.left() || p.x == r.right())
                    && p.y >= r.bottom() && p.y <= r.top();
            boolean onHorizontalWall = (p.y == r.bottom() || p.y == r.top())
                    && p.x >= r.left() && p.x <= r.right();
            if (onVerticalWall || onHorizontalWall) {
                return i;
            }
        }
        return -1;
    }

    @Test(timeout = 5000)
    public void connectsThreeSeparatedRooms() {
        GameMap map = emptyMap();

        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room(0, 5, 5, 2, 2));
        rooms.add(new Room(1, 5, 5, 15, 2));
        rooms.add(new Room(2, 5, 5, 28, 12));

        for (Room room : rooms) {
            markRoom(map, room);
        }

        HallwayGenerator generator = new HallwayGenerator(
                WIDTH, HEIGHT, new RandomGen(12345));
        List<Hallway> hallways = new ArrayList<>();
        generator.generate(map, rooms, hallways);

        assertTrue("expected at least one hallway per tree edge",
                hallways.size() >= rooms.size() - 1);

        DSU roomComponents = new DSU(rooms.size());
        for (Hallway hallway : hallways) {
            assertTrue(hallway.points.size() >= 2);

            Point first = hallway.points.get(0);
            Point last = hallway.points.get(hallway.points.size() - 1);
            int roomA = roomAtDoor(first, rooms);
            int roomB = roomAtDoor(last, rooms);

            assertTrue("hallway does not start at a room door: " + first.x + "," + first.y,
                    roomA >= 0);
            assertTrue("hallway does not end at a room door: " + last.x + "," + last.y,
                    roomB >= 0);

            roomComponents.union(roomA, roomB);

            for (Point p : hallway.points) {
                assertTrue(p.x >= 0 && p.x < WIDTH);
                assertTrue(p.y >= 0 && p.y < HEIGHT);
                assertEquals(TileType.HALLWAY, map.getTileType(p.x, p.y));
            }
        }

        for (int i = 1; i < rooms.size(); i++) {
            assertTrue("room " + i + " is not connected",
                    roomComponents.isConnected(0, i));
        }
    }
}
