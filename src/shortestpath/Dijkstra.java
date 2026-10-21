package shortestpath;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
public class Dijkstra {
    public static final int INF = Integer.MAX_VALUE;

    public final int source;
    public final int[] dist;
    public final int[] parent;
    public long edgesRelaxed = 0;
    public long pqOperations = 0;

    private Dijkstra(int n, int source) {
        this.source = source;
        this.dist = new int[n];
        this.parent = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
    }

    public static Dijkstra run(Graph g, int source) {
        for (Edge e : g.getEdges()) {
            if (e.weight < 0) {
                throw new IllegalArgumentException("Dijkstra does not support negative edge weights: " + e);
            }
        }
        Dijkstra res = new Dijkstra(g.getN(), source);
        // queue element = {vertex, distance when it was pushed}
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));
        res.dist[source] = 0;
        pq.add(new int[]{source, 0});
        res.pqOperations++;

        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            res.pqOperations++;
            int u = top[0];
            if (top[1] > res.dist[u]) {
                continue;
            }
            for (Edge e : g.getAdj(u)) {
                res.edgesRelaxed++;
                int newDist = res.dist[u] + e.weight;
                if (newDist < res.dist[e.to]) {
                    res.dist[e.to] = newDist;
                    res.parent[e.to] = u;
                    pq.add(new int[]{e.to, newDist});
                    res.pqOperations++;
                }
            }
        }
        return res;
    }

    public boolean isReachable(int v) {
        return dist[v] != INF;
    }

    public int reachableCount() {
        int c = 0;
        for (int d : dist) {
            if (d != INF) {
                c++;
            }
        }
        return c;
    }

    public List<Integer> getPath(int target) {
        List<Integer> path = new ArrayList<>();
        if (!isReachable(target)) {
            return path;
        }
        for (int v = target; v != -1; v = parent[v]) {
            path.add(v);
        }
        Collections.reverse(path);
        return path;
    }
}
