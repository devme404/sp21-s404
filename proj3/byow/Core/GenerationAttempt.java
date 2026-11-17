package byow.Core;

public class GenerationAttempt {
    private final Tile[][] tiles;
    private int width, height;

    GenerationAttempt(int width, int height) {
        this.width = width;
        this.height = height;
        tiles = new Tile[width][height];

        initialize();
    }

    private void initialize() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile(TileType.EMPTY);
            }
        }
    }

    public void set(int x, int y, TileType type) {
        tiles[x][y].set(type);
    }

    public TileType get(int x, int y) {
        return tiles[x][y].get();
    }

    public boolean inBound(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isPassable(int x, int y) {
        if (!inBound(x, y)) {
            return false;
        }
        return tiles[x][y].get() == TileType.EMPTY || tiles[x][y].get() == TileType.HALLWAY;
    }

    public Tile[][] getTiles() {
        return tiles;
    }
};
