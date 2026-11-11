package byow.Core;

import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

public class HallwayPathFinder {
    private static final int[][] adj = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    public static boolean findPath(List<Point> path, GameMap map) {
        Point start = path.get(0);
        Point end = path.get(1);

        boolean[][] visited = new boolean[map.width][map.height];
        Map<Point, Point> parents = new HashMap<>();
        Queue<Point> queue = new LinkedList<>();

        visited[start.x][start.y] = true;
        parents.put(start, null);
        queue.offer(start);

        boolean found = false;

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            if (p.equals(end)) {
                found = true;
                break;
            }


            for (int[] d : adj) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];

                if (nx < 1 || nx >= map.width - 1 || ny < 1 || ny >= map.height - 1) {
                    continue;
                }
                if (visited[nx][ny]) {
                    continue;
                }

                Point np = new Point(nx, ny);
                if (!np.equals(end) && !passable(map, nx, ny)) {
                    continue;
                }

                visited[nx][ny] = true;
                queue.offer(np);
                parents.put(np, p);
            }
        }

        if (!found) {
            return false;
        }

        List<Point> result = new LinkedList<>();
        for (Point cur = end; cur != null; cur = parents.get(cur)) {
            result.addFirst(cur);
            if (cur.equals(start)) {
                break;
            }
        }
        if (result.isEmpty() || !result.getFirst().equals(start)) {
            return false;
        }
        path.clear();
        path.addAll(result);

        for (Point p : result) {
            map.setTileType(p.x, p.y, TileType.HALLWAY);
        }
        return true;
    }

    private static boolean passable(GameMap map, int x, int y) {
        TileType t = map.getTileType(x, y);
        return t == TileType.EMPTY || t == TileType.HALLWAY;
    }
}
