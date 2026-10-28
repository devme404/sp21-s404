package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Test {
    public static void main(String args[]) {
        Engine engine = new Engine();
        TERenderer ter = new TERenderer();
        ter.initialize(80, 30);
        TETile[][] world = engine.interactWithInputString(args[0]);
        ter.renderFrame(world);
    }
}
