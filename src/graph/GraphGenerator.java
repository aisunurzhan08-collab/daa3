package graph;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
public class GraphGenerator {

    private static final int MAX_WEIGHT = 100;
    public static Graph connectedUndirected(int v, int e, long seed) {
        Random rnd = new Random(seed);
        Graph g = new Graph(v, false);
        Set<Long> used = new HashSet<>();
        for (int i = 1; i < v; i++) {
            int j = rnd.nextInt(i);
            g.addEdge(i, j, 1 + rnd.nextInt(MAX_WEIGHT));
            used.add(key(i, j, false, v));
        }
        while (g.getEdgeCount() < e) {
            int a = rnd.nextInt(v);
            int b = rnd.nextInt(v);
            if (a == b || !used.add(key(a, b, false, v))) {
                continue;
            }
            g.addEdge(a, b, 1 + rnd.nextInt(MAX_WEIGHT));
        }
        return g;
    }
    public static Graph directed(int v, int e, boolean dag, long seed) {
        Random rnd = new Random(seed);
        Graph g = new Graph(v, true);
        Set<Long> used = new HashSet<>();
        while (g.getEdgeCount() < e) {
            int a = rnd.nextInt(v);
            int b = rnd.nextInt(v);
            if (a == b) {
                continue;
            }
            if (dag && a > b) {
                int t = a;
                a = b;
                b = t;
            }
            if (!used.add(key(a, b, true, v))) {
                continue;
            }
            g.addEdge(a, b, 1 + rnd.nextInt(MAX_WEIGHT));
        }
        return g;
    }
    private static long key(int a, int b, boolean directed, int v) {
        if (!directed && a > b) {
            int t = a;
            a = b;
            b = t;
        }
        return (long) a * v + b;
    }
}
