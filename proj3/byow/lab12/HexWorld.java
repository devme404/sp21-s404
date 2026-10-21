package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 80;
    private static final int HEIGHT = 80;

    private static final long SEED = 23848130;
    private static final Random RANDOM = new Random(SEED);

    private static TETile randomTile() {
        int tile_num = RANDOM.nextInt(6);
        switch (tile_num) {
            case 0: return Tileset.GRASS;
            case 1: return Tileset.WATER;
            case 2: return Tileset.FLOWER;
            case 3: return Tileset.SAND;
            case 4: return Tileset.MOUNTAIN;
            case 5: return Tileset.TREE;
            default: return Tileset.NOTHING;
        }
    }

    private static void printRow(TETile[][] tiles, TETile tile, int x_off, int y_off, int space_num, int char_num) {
        for (int i = 0; i < char_num; i++) {
            tiles[x_off + i + space_num][y_off] = tile;
        }
    }

    // paras[i]: the ith row of the hexagon, from bottom to top
    // paras[][0]: the number of spaces
    // paras[][1]: the number of characters
    private static int[][] hexParas(int size, int x_off) {
        int[][] paras = new int[2 * size][2];
        for (int i = 0, chars = size; i < size && chars < 3 * size - 1; i++, chars += 2) {
            paras[i][0] = size - i - 1;
            paras[2 * size - i - 1][0] = size - i - 1;
            paras[i][1] = chars;
            paras[2 * size - i - 1][1] = chars;
        }
        return paras;
    }

    private static void addHexagon(TETile[][] tiles, int size, int x_off, int y_off, TETile tile) {
        int[][] paras = hexParas(size, x_off);
        for (int i = 0; i < 2 * size; i++) {
            printRow(tiles, tile, x_off, y_off + i, paras[i][0], paras[i][1]);
        }

    }

    private static int[] coorTrans(int dx, int dy, int x, int y, int x_off, int y_off) {
        int[] result = new int[2];
        result[0] = x_off + x * dx;
        result[1] = y_off + y * dy;
        return result;
    }

    private static int[][] tessRow(int dx, int dy, int x, int y, int x_off, int y_off, int row_size)  {
        int[][] result = new int[row_size][2];
        for (int i = 0; i < row_size; i++) {
            result[i] = coorTrans(dx, dy, x + i, y, x_off, y_off);
        }
        return result;
    }

    private static int[][] tessCoors(int[] rows_size, int size, int x_off, int y_off) {
        int hex_nums = 0;
        for (int row_size : rows_size) {
            hex_nums += row_size;
        }
        int[][] result = new int[hex_nums][2];

        int dx = 4 * size - 2;
        int dy = size;
        int next = 0;
        int[][] row = null;
        
        for (int i = 0; i < rows_size.length; i++) {
            switch(rows_size[i]) {
                case 1:
                    row = tessRow(dx, dy, 0, i, x_off + 4 * size - 2, y_off, 1);
                    result[next] =  row[0];
                    next++;
                    break;
                case 2:
                    row = tessRow(dx, dy, 0, i, x_off + 2 * size - 1, y_off, 2);
                    result[next] = row[0];
                    next++;
                    result[next] = row[1];
                    next++;
                    break;
                case 3:
                    row = tessRow(dx, dy, 0, i, x_off, y_off, 3);
                    for (int j = 0; j < 3; j++) {
                        result[next] = row[j];
                        next++;
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    private static void addTess(TETile[][] tiles, int size, int x_off, int y_off) {
        int[] rows_size = {1, 2, 3, 2, 3, 2, 3, 2, 1};
        int[][] hex_coors = tessCoors(rows_size, 3, x_off, y_off);
        for (int[] hex_coor : hex_coors) {
            addHexagon(tiles, size, hex_coor[0], hex_coor[1], randomTile());
        }
    }


    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] tiles = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
        // for (int size = 2; size < 6; size++) {
        //     addHexagon(tiles, size, 0, (size + 2) * (size - 1), randomTile());
        // }
        addTess(tiles, 3, 0, 0);

        ter.renderFrame(tiles);
    }
}
