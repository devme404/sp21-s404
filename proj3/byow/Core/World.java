package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.Core.RandomUtils;

import java.util.Random;

public class World {
    // Magic numbers to decide some maximum values for rooms and hallways
    // Divide the minimum one between the width and the height by these numbers to get corresponding values
    private static final int ROOM_MAX_SIZE_RATIO = 3;
    private static final int HALLWAY_MAX_LENGTH_RATIO = 1;
    private static final int ROOM_MAX_NUM_RATIO = 5;
    private static final int HALLWAY_MAX_NUM_RATIO = 4;
    private static final int MAX_TRIES = 10;

    public enum SpaceType {
        ROOM, HALLWAY;
    }

    // The unit we use to generate the world
    public class Space {
        SpaceType type;
        public int width;
        public int height;
        public int left_bottom_x;
        public int left_bottom_y;

        Space(SpaceType type) {
            this.type = type;
            switch (this.type) {
                case ROOM:
                    this.width = random.nextInt(room_max_size) + 1;
                    this.height = random.nextInt(room_max_size) + 1;
                    break;
                case HALLWAY:
                    // Decide whether the hallway is horizontal or vertical
                    // Then limit its width or height to meet the requirement
                    boolean is_horizontal = (random.nextInt(2) == 0);
                    if (is_horizontal) {
                        this.width = random.nextInt(hallway_max_length);
                        this.height = random.nextInt(2) + 1;
                    } else {
                        this.width = random.nextInt(2) + 1;
                        this.height = random.nextInt(hallway_max_length);
                    }
                    break;
                default:
                    break;
            }

        }
    }

    // Basic variables of the world
    private int width;
    private int height;
    private long seed;
    private Random random;
    private TETile[][] world;

    // Variables about rooms and hallways
    private int room_max_size;
    private int hallway_max_length;
    private int room_num;
    private int hallway_num;
    private Space[] spaces;
    private int space_idx;

    // Variables about the player
    private int player_x;
    private int player_y;

    World(int width, int height, long seed, TETile[][] world) {
        setSeed(seed);
        setSize(width, height);
        initializeWorld(world);
    }

    // Set the width and height of the world
    // Then calculate other values depend on them
    private void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        int min_size = (this.width < this.height) ? this.width : this.height;

        // Decide the max size of rooms and hallways according to the actual width or height
        room_max_size = min_size / ROOM_MAX_SIZE_RATIO;
        hallway_max_length = min_size / HALLWAY_MAX_LENGTH_RATIO;

        // Decide the max number of rooms and hallways according to the actual width or height
        room_num = random.nextInt(min_size / ROOM_MAX_NUM_RATIO) + 1;
        hallway_num = random.nextInt(min_size / HALLWAY_MAX_NUM_RATIO - room_num) + room_num;

        spaces = new Space[room_num + hallway_num];
        space_idx = 0;
    }

    public void setSeed(long seed) {
        this.seed = seed;
        random = new Random(this.seed);
    }

    private void initializeWorld(TETile[][] world) {
        this.world = world;
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.world[x][y] = Tileset.NOTHING;
            }
        }
    }
    
    private void initializePlayer() {
        player_x = 0;
        player_y = 0;
    }

    public void generate() {
        int generated_room_num = 0;
        int generated_hallway_num = 0;

        while (generated_room_num < room_num) {
            generateRoom();
            generated_room_num++;
        }
    }
    
    private void drawRoom(Space space) {
        for (int dx = 0; dx < space.width; dx++) {
            this.world[space.left_bottom_x + dx][space.left_bottom_y] = Tileset.WALL;
            this.world[space.left_bottom_x + dx][space.left_bottom_y + space.height] = Tileset.WALL;
        }
        for (int dy = 0; dy < space.height; dy++) {
            this.world[space.left_bottom_x][space.left_bottom_y + dy] = Tileset.WALL;
            this.world[space.left_bottom_x + space.width][space.left_bottom_y + dy] = Tileset.WALL;
        }
        this.world[space.left_bottom_x + space.width][space.left_bottom_y + space.height] = Tileset.WALL;

        for (int dx = 1; dx < space.width; dx++) {
            for (int dy = 1; dy < space.height; dy++) {
                this.world[space.left_bottom_x + dx][space.left_bottom_y + dy] = Tileset.FLOOR;
            }
        }
    }

    private void generateRoom() {
        Space room = new Space(SpaceType.ROOM);
        boolean is_valid = false;
        int tries = 0;
        // Constantly sample until we find a valid room
        while (!is_valid) {
            if (tries > MAX_TRIES) {
                throw new RuntimeException("This seed is failed:" + this.seed);
            }
            room.left_bottom_x = random.nextInt(this.width);
            room.left_bottom_y = random.nextInt(this.height);
            is_valid = isRoomValid(room);
            tries++;
        }
        drawRoom(room);
        spaces[space_idx] = room;
        space_idx++;
    }

    private void drawHallway(Space hallway) {
    }
    
    private void generateHallway() {
        Space hallway = new Space(SpaceType.HALLWAY);
        boolean is_valid = false;
        while (!is_valid) {
            hallway.left_bottom_x = random.nextInt(this.width);
            hallway.left_bottom_y = random.nextInt(this.height);
            is_valid = isHallwayValid(hallway);
        }
        drawHallway(hallway);
        spaces[space_idx] = hallway;
        space_idx++;
    }

    private boolean isIntervalOverlapped(int left1, int size1, int left2, int size2) {
        return left1 <= left2 + size2 && left2 <= left1 + size1;
    }

    private boolean isIntervalIntersected(int left1, int size1, int left2, int size2) {
        return left1 < left2 + size2 && left2 < left1 + size1;
    }

    private boolean isRoomValid(Space room) {
        if (room.left_bottom_x < 0 || room.left_bottom_x + room.width >= this.width || room.left_bottom_y < 0 || room.left_bottom_y + room.height >= this.height) {
            return false;
        }

        for (Space space : spaces) {
            if (space == null) {
                break;
            }
            if (isSpacesOverlapped(space, room)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSpacesOverlapped(Space space1, Space space2) {
        return isIntervalOverlapped(space1.left_bottom_x, space1.width, space2.left_bottom_x, space2.width)
            && isIntervalOverlapped(space1.left_bottom_y, space1.height, space2.left_bottom_y, space2.height);
    }

    private boolean isHallwayValid(Space hallway) {
        if (hallway.left_bottom_x < 0 || hallway.left_bottom_x + hallway.width >= this.width || hallway.left_bottom_y < 0 || hallway.left_bottom_y + hallway.height >= this.height) {
            return false;
        }

        for (Space space : spaces) {
            if (space == null) {
                break;
            }
            if (isSpacesIntersected(space, hallway)) {
                return false;
            }
        }

        // Check whether the hallway is connected to another space
        // If the hallway is horizontal, check its left and right side
        if (hallway.width >= hallway.height) {
            for (int dy = 0; dy < hallway.height; dy++) {
                if (this.world[hallway.left_bottom_x][hallway.left_bottom_y + dy] != Tileset.WALL || this.world[hallway.left_bottom_x + hallway.width][hallway.left_bottom_y + dy] != Tileset.WALL) {
                    return false;
                }
            }
        } else {
            for (int dx = 0; dx < hallway.width; dx++) {
                if (this.world[hallway.left_bottom_x + dx][hallway.left_bottom_y] != Tileset.WALL || this.world[hallway.left_bottom_x + dx][hallway.left_bottom_y + hallway.height] != Tileset.WALL) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isSpacesIntersected(Space space1, Space space2) {
        return isIntervalIntersected(space1.left_bottom_x, space1.width, space2.left_bottom_x, space2.width)
            && isIntervalIntersected(space1.left_bottom_y, space1.height, space2.left_bottom_y, space2.height);
    }

    public void move(char key) {
        int new_player_x = player_x;
        int new_player_y = player_y;
        switch (key) {
            case 'w':
                new_player_y++;
                break;
            case 's':
                new_player_y--;
                break;
            case 'a':
                new_player_x--;
                break;
            case 'd':
                new_player_x++;
                break;
            default:
                break;
        }

        if (positionValid(new_player_x, new_player_y)) {
            player_x = new_player_x;
            player_y = new_player_y;
        }
    }

    boolean positionValid(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            return false;
        }

        if (this.world[x][y] != Tileset.FLOOR) {
            return false;
        }

        return true;
    }
}
