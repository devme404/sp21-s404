package byow.Core;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Comparator;

public class HallwayGenerator {
    private static final int HALLWAY_MAX_LENGTH_RATIO = 1;
    private static final int HALLWAY_MAX_NUM_RATIO = 9;
    private static final int MAX_TRIES = 100;

    private int width, height;
    private RandomGen random;
    private int hallway_max_length, hallway_num;
private class Edge {
        int a, b;
        double distance;

        Edge(int a, int b) {
            this.a = a;
            this.b = b;
            this.distance = -1;
        }

        Edge(int a, int b, double distance) {
            this.a = a;
            this.b = b;
            this.distance = distance;
        }
    };


    HallwayGenerator(int width, int height, RandomGen random) {
        this.width = width;
        this.height = height;
        this.random = random;
        int min_size = (width < height) ? width : height;

        hallway_max_length = min_size / HALLWAY_MAX_LENGTH_RATIO;
        hallway_num = random.numberTo(min_size, HALLWAY_MAX_NUM_RATIO);
    }

    void generate(GameMap map, List<Room> rooms, List<Hallway> hallways) {
        if (rooms.size() < 2) {
            return;
        }

        connectRooms(map, rooms, hallways);
        addExtraHallways(map, rooms, hallways);
    }

    private void connectRooms(GameMap map, List<Room> rooms, List<Hallway> hallways) {
        DSU dsu = new DSU(rooms.size());
        List<Edge> edges = allEdgesSortedByDistance(rooms);
        int joined = 0;

        for (Edge edge : edges) {
            if (dsu.isConnected(edge.a, edge.b)) {
                continue;
            }

            Hallway hallway = generateHallway(edge, rooms, map, hallways);
            if (hallway == null) {
                continue;
            }

            hallways.add(hallway);
            dsu.union(edge.a, edge.b);
            joined++;

            if (joined == rooms.size() - 1) {
                return;
            }
        }

        throw new RuntimeException("Failed to connect all rooms.");
    }

    private void addExtraHallways(GameMap map, List<Room> rooms, List<Hallway> hallways) {
        for (int i = 0; i < hallway_num; i++) {
            int a = random.intTo(rooms.size());
            int b = random.intTo(rooms.size());
            if (a == b) {
                continue;
            }

            Hallway hallway = generateHallway(new Edge(a, b), rooms, map, hallways);
            if (hallway == null) {
                continue;
            }

            hallways.add(hallway);
        }
    }


    private List<Point> randomDoors(Room a, Room b, RandomGen random) {
        List<Point> path = new ArrayList<>();
        randomDoor(path, a, a.sideToward(b), random);
        randomDoor(path, b, b.sideToward(a), random);
        return path;
    }

    private void randomDoor(List<Point> path, Room r, Side s, RandomGen random) {
        switch(s) {
            case LEFT:
                path.add(new Point(r.left(), random.sampleEdge(r.bottom(), r.top())));
                break;
            case RIGHT:
                path.add(new Point(r.right(), random.sampleEdge(r.bottom(), r.top())));
                break;
            case TOP:
                path.add(new Point(random.sampleEdge(r.left(), r.right()), r.top()));
                break;
            case BOTTOM:
                path.add(new Point(random.sampleEdge(r.left(), r.right()), r.bottom()));
                break;
            default:
                break;
        }
    }

    private Hallway generateHallway(Edge edge, List<Room> rooms, GameMap map, List<Hallway> hallways) {
        int tries = 0;
        boolean success = false;
        List<Point> path = null;
        while (tries < MAX_TRIES && !success) {
            path = randomDoors(rooms.get(edge.a), rooms.get(edge.b), random);
            success = HallwayPathFinder.findPath(path, map);
            tries++;
        }
        return (success) ? new Hallway(hallways.size(), 1, path) : null;
    }

    private Point center(Room r) {
        int x = r.centerX();
        int y = r.centerY();
        return new Point(x, y);
    }

    // The distance between the edges of the two rooms.
    private double distance(Room a, Room b) {
        int dx = Math.max(0, Math.max(a.left() - b.right(), b.left() - a.right()));
        int dy = Math.max(0, Math.max(a.bottom() - b.top(), b.bottom() - a.top()));
        double result = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        return result;
    }

    // Just list all the edges and sort them by length.
    private List<Edge> allEdgesSortedByDistance(List<Room> rooms) {
        List<Edge> result = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            Room a = rooms.get(i);
            for (int j = i + 1; j < rooms.size(); j++) {
                Room b = rooms.get(j);
                result.add(new Edge(i, j, distance(a, b)));
            }
        }
        result.sort(Comparator.comparingDouble(Edge -> Edge.distance));
        return result;
    }
}
