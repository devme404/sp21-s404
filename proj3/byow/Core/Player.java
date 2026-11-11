package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Player {
    int x, y;

    Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Player(Player other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Player moved(int dx, int dy) {
        return new Player(x + dx, y + dy);
    }


}
