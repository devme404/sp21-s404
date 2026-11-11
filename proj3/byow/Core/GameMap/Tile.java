package byow.Core;

public class Tile {
    private TileType type;

    Tile() {
        this.type = TileType.EMPTY;
    }

    Tile(TileType type) {
        set(type);
    }

    public TileType get() {
        return type;
    }

    public void set(TileType type) {
        this.type = type;
    }

    public boolean empty() {
        return type == TileType.EMPTY;
    }
}
