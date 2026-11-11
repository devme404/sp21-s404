package byow.Core;

import java.util.List;

public final class Hallway {
    final int id, width;
    final List<Point> points;

    Hallway(int id, int width, List<Point> points) {
        this.id = id;
        this.width = width;
        this.points = points;
    }
}
