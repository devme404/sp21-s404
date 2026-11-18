package byow.Core;

import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

public class HallwayPathFinder {
    private static final int[][] adj = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

    // Use BFS to find a valid path between the two rooms.
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

                // This condition isn't intuitive at first glance: it first checks whether the
                // tile is the end, and then whether the tile is passable, since the end is not
                // passable.
                if (!np.equals(end) && !map.isPassable(nx, ny)) {
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

        // Reconstruct the path by tracking the parent tile.
        LinkedList<Point> result = new LinkedList<>();
        for (Point cur = end; cur != null; cur = parents.get(cur)) {
            result.addFirst(cur);
            if (cur.equals(start)) {
                break;
            }
        }

        // Discard the invalid path.
        if (result.isEmpty() || !result.getFirst().equals(start)) {
            return false;
        }

        path.clear();
        path.addAll(result);

        for (Point p : result) {
            map.setLogicalTileType(p.x, p.y, TileType.HALLWAY);
        }
        return true;
    }
}
