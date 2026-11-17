package byow.Core;

import java.util.Map;
import java.util.HashMap;

public class DSU {
    private final int[] parent;
    private final int[] size;

    DSU(int size) {
        parent = new int[size];
        this.size = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
            this.size[i] = 1;
        }
    }

    public void add(int n) {
        parent[n] = n;
        size[n] = 1;
    }

    public int find(int x) {
        int p = parent[x];
        if (p != x) {
            parent[x] = find(p);
        }

        return parent[x];
    }

    public boolean union(int x, int y) {
        int root_x = find(x);
        int root_y = find(y);

        if (root_x == root_y) {
            return false;
        }

        if (size[root_x] < size[root_y]) {
            int temp = root_x;
            root_x = root_y;
            root_y = temp;
        }
        parent[root_y] = root_x;
        size[root_x] = size[root_x] + size[root_y];

        return true;
    }

    public boolean isConnected(int x, int  y) {
        return find(x) == find(y);
    }

    public int size(int  x) {
        return size[find(x)];
    }
}
