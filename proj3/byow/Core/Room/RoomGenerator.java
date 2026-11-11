package byow.Core;

import java.util.List;

public class RoomGenerator {
    private static final int ROOM_MAX_SIZE_RATIO = 4;
    private static final int ROOM_MAX_NUM_RATIO = 3;
    private static final int MAX_TRIES = 100;
    private int width, height;
    private int room_max_size, room_num;
    private RandomGen random;

    RoomGenerator(int width, int height, RandomGen random) {
        this.width = width;
        this.height = height;
        this.random = random;
        int min_size = (this.width < this.height) ? this.width : this.height;

        room_max_size = min_size / ROOM_MAX_SIZE_RATIO;
        room_num = random.numberTo(min_size, ROOM_MAX_NUM_RATIO);
    }

    void generate(GameMap map, List<Room> rooms) {
        int generated_room_num = 0;

        while (generated_room_num < room_num) {
            Room room = tryPlaceRoom(map, rooms);
            generated_room_num++;
            if (room == null) {
                continue;
            }
            placeRoom(map, room);
            rooms.add(room);
        }
    }

    private Room tryPlaceRoom(GameMap map, List<Room> rooms) {
        Room room = null;
        boolean is_valid = false;
        int tries = 0;

        // Constantly sample until we find a valid room
        while (!is_valid) {
            if (tries > MAX_TRIES) {
                return null;
            }
            room = new Room(rooms.size(), random.roomSize(room_max_size), random.roomSize(room_max_size), random.intTo(width), random.intTo(height));
            is_valid = isRoomValid(room, rooms);
            tries++;
        }

        return room;
    }

    private void placeRoom(GameMap map, Room room) {
        for (int dx = 0; dx < room.width; dx++) {
            for (int dy = 0; dy < room.height; dy++) {
                map.setTileType(room.left() + dx, room.bottom() + dy, TileType.ROOM);
            }
        }
    }

    private boolean isRoomValid(Room room, List<Room> rooms) {
        if (room.left() < 1 || room.right() >= width - 1 || room.bottom() <= 0 || room.top() >= height - 1) {
            return false;
        }

        for (Room r : rooms) {
            if (room.left() <= r.right() + 1 && r.left() <= room.right() + 1
                    && room.bottom() <= r.top() + 1 && r.bottom() <= room.top() + 1) {
                return false;
            }
        }
        return true;
    }
}
