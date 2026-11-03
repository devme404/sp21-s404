package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Player {
    private static final int MAX_TRIES = 100;
    int x, y;

    Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Player(Player other) {
        this.x = other.x;
        this.y = other.y;
    }

    Player(WorldGenerator generator, Map map) {
        intialize(generator, map);
    }

    private void intialize(WorldGenerator generator, Map map) {
        int tries = 0;
        while (tries < MAX_TRIES) {
            Player new_player = generator.intializePlayer();
            tries++;
            if (positionValid(new_player, map)) {
                setPosition(new_player);
                return;
            }
        }
    }

    public void move(char key, Map map) {
        Player new_player = new Player(this);
        switch (key) {
            case 'w':
                new_player.y++;
                break;
            case 's':
                new_player.y--;
                break;
            case 'a':
                new_player.x--;
                break;
            case 'd':
                new_player.x++;
                break;
            default:
                break;
        }

        if (positionValid(new_player, map)) {
            setPosition(new_player);
        }
    }

    private void setPosition(Player new_player) {
        x = new_player.x;
        y = new_player.y;
    }

    private boolean positionValid(Player player, Map map) {
        if (player.x < 0 || player.x >= map.width || player.y < 0 || player.y >= map.height) {
            return false;
        }

        if (!map.isWalkable(player.x, player.y)) {
            return false;
        }

        return true;
    }
}
