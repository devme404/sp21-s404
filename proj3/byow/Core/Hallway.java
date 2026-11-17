package byow.Core;

import java.util.List;

public final class Hallway {
    final int id;
    int width;
    final List<Point> points;
    List<Point> addition;

    Hallway(int id, int width, List<Point> points) {
        this.id = id;
        this.width = width;
        this.points = points;
        this.addition = null;
    }
}
