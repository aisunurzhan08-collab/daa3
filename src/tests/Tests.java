package tests;
import connectivity.SCC;
import connectivity.TopologicalSort;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class Tests {
    static int passed = 0, failed = 0;
    static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }
    static Graph undirected(int n, int[][] edges) {
        Graph g = new Graph(n, false);
        for (int[] e : edges) {
            g.addEdge(e[0], e[1], e[2]);
        }
        return g;
    }
    static Graph directed(int n, int[][] edges) {
        Graph g = new Graph(n, true);
        for (int[] e : edges) {
            g.addEdge(e[0], e[1], e[2]);
        }
        return g;
    }

    public static void main(String[] args) {
        testMst();
        testTopological();
        testScc();
        testDijkstra();
        System.out.println("\nPassed: " + passed + ", Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void testMst() {
        System.out.println("--- MST ---");
        Graph small = undirected(4, new int[][]{{0, 1, 1}, {1, 2, 2}, {2, 3, 3}, {0, 3, 10}, {0, 2, 4}});
        check("small graph: Kruskal weight = 6", Kruskal.run(small).totalWeight == 6);
        check("small graph: Prim weight = 6", Prim.run(small).totalWeight == 6);
        Graph disc = undirected(5, new int[][]{{0, 1, 1}, {2, 3, 2}});
        MSTResult k = Kruskal.run(disc), p = Prim.run(disc);
        check("disconnected: Kruskal detects it", !k.connected && k.components == 3);
        check("disconnected: Prim detects it", !p.connected && p.components == 3);
        check("disconnected: both give forest weight 3", k.totalWeight == 3 && p.totalWeight == 3);
        Graph tri = undirected(3, new int[][]{{0, 1, 1}, {1, 2, 2}, {0, 2, 3}});
        check("cycle (triangle): weight = 3, 2 edges",
                Kruskal.run(tri).totalWeight == 3 && Prim.run(tri).totalWeight == 3 && Kruskal.run(tri).edges.size() == 2);
        Graph square = undirected(4, new int[][]{{0, 1, 1}, {1, 2, 1}, {2, 3, 1}, {3, 0, 1}});
        check("multiple MSTs (square): both give weight 3",
                Kruskal.run(square).totalWeight == 3 && Prim.run(square).totalWeight == 3);
        Graph dup = undirected(4, new int[][]{{0, 1, 5}, {0, 2, 5}, {0, 3, 5}, {1, 2, 5}, {1, 3, 5}, {2, 3, 5}});
        check("duplicate weights (K4): both give weight 15",
                Kruskal.run(dup).totalWeight == 15 && Prim.run(dup).totalWeight == 15);
        Graph one = new Graph(1, false);
        check("single vertex: weight 0, connected",
                Kruskal.run(one).totalWeight == 0 && Kruskal.run(one).connected
                        && Prim.run(one).totalWeight == 0 && Prim.run(one).connected);

        boolean same = true;
        for (int seed = 0; seed < 200; seed++) {
            Graph g = GraphGenerator.connectedUndirected(30, 80, seed);
            MSTResult a = Kruskal.run(g), b = Prim.run(g);
            if (a.totalWeight != b.totalWeight || !a.connected || !b.connected
                    || a.edges.size() != 29 || b.edges.size() != 29) {
                same = false;
            }
        }
        check("200 random connected graphs: Kruskal weight == Prim weight, V-1 edges", same);
    }

    static boolean validOrder(Graph g, List<Integer> order) {
        if (order.size() != g.getN()) {
            return false;
        }
        int[] pos = new int[g.getN()];
        for (int i = 0; i < order.size(); i++) {
            pos[order.get(i)] = i;
        }
        for (Edge e : g.getEdges()) {
            if (pos[e.from] >= pos[e.to]) {
                return false;
            }
        }
        return true;
    }

    static void testTopological() {
        System.out.println("--- Topological sort ---");
        Graph dag = directed(6, new int[][]{{5, 2, 1}, {5, 0, 1}, {4, 0, 1}, {4, 1, 1}, {2, 3, 1}, {3, 1, 1}});
        TopologicalSort t = TopologicalSort.run(dag);
        check("small DAG: valid order", t.isDag && validOrder(dag, t.order));
        Graph cyc = directed(3, new int[][]{{0, 1, 1}, {1, 2, 1}, {2, 0, 1}});
        check("cycle 0->1->2->0: detected, no ordering", !TopologicalSort.run(cyc).isDag);
        Graph one = new Graph(1, true);
        check("single vertex: order = [0]", TopologicalSort.run(one).order.equals(List.of(0)));

        boolean allValid = true;
        for (int seed = 0; seed < 100; seed++) {
            Graph g = GraphGenerator.directed(50, 200, true, seed);
            TopologicalSort r = TopologicalSort.run(g);
            if (!r.isDag || !validOrder(g, r.order)) {
                allValid = false;
            }
        }
        check("100 random DAGs: order is valid every time", allValid);

        boolean cycOk = true;
        for (int seed = 0; seed < 100; seed++) {
            Graph g = GraphGenerator.directed(50, 200, false, seed);
            boolean dagByTopo = TopologicalSort.run(g).isDag;
            boolean dagByScc = SCC.run(g).components.size() == g.getN();
            if (dagByTopo != dagByScc) {
                cycOk = false;
            }
        }
        check("100 random directed graphs: topological sort agrees with SCC about cycles", cycOk);
    }
    static void testScc() {
        System.out.println("--- SCC ---");
        // {0,1,2} is a cycle, {3,4,5} is a cycle, 2 -> 3 connects them
        Graph g = directed(6, new int[][]{{0, 1, 1}, {1, 2, 1}, {2, 0, 1}, {2, 3, 1}, {3, 4, 1}, {4, 5, 1}, {5, 3, 1}});
        SCC scc = SCC.run(g);
        Set<Set<Integer>> found = new HashSet<>();
        for (List<Integer> c : scc.components) {
            found.add(new HashSet<>(c));
        }
        Set<Set<Integer>> expected = Set.of(Set.of(0, 1, 2), Set.of(3, 4, 5));
        check("two cycles joined by one edge: 2 SCCs {0,1,2} and {3,4,5}", found.equals(expected));
        check("single vertex: 1 SCC", SCC.run(new Graph(1, true)).components.size() == 1);
        Graph dag = GraphGenerator.directed(20, 40, true, 1);
        check("DAG: every vertex is its own SCC", SCC.run(dag).components.size() == 20);

        boolean exactlyOnce = true;
        for (int seed = 0; seed < 100; seed++) {
            Graph r = GraphGenerator.directed(40, 90, false, seed);
            int[] count = new int[40];
            for (List<Integer> c : SCC.run(r).components) {
                for (int v : c) {
                    count[v]++;
                }
            }
            for (int c : count) {
                if (c != 1) {
                    exactlyOnce = false;
                }
            }
        }
        check("100 random graphs: every vertex is in exactly one SCC", exactlyOnce);
    }
    static void testDijkstra() {
        System.out.println("--- Dijkstra ---");
        Graph g = undirected(5, new int[][]{{0, 1, 4}, {0, 2, 1}, {2, 1, 2}, {1, 3, 1}, {2, 3, 5}});
        Dijkstra d = Dijkstra.run(g, 0);
        check("small graph: distances 0,3,1,4", Arrays.equals(Arrays.copyOf(d.dist, 4), new int[]{0, 3, 1, 4}));
        check("small graph: path to 3 is 0,2,1,3", d.getPath(3).equals(List.of(0, 2, 1, 3)));
        check("vertex 4 is unreachable (INF, empty path)", d.dist[4] == Dijkstra.INF && d.getPath(4).isEmpty());
        Graph one = new Graph(1, false);
        check("single vertex: distance 0", Dijkstra.run(one, 0).dist[0] == 0);

        boolean threw = false;
        try {
            Dijkstra.run(directed(2, new int[][]{{0, 1, -5}}), 0);
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("negative weight is rejected", threw);

        boolean ok = true;
        for (int seed = 0; seed < 100; seed++) {
            int n = 15;
            Graph r = GraphGenerator.directed(n, 25, false, seed);
            long[][] fw = new long[n][n];
            for (long[] row : fw) {
                Arrays.fill(row, Long.MAX_VALUE / 4);
            }
            for (int i = 0; i < n; i++) {
                fw[i][i] = 0;
            }
            for (Edge e : r.getEdges()) {
                fw[e.from][e.to] = Math.min(fw[e.from][e.to], e.weight);
            }
            for (int m = 0; m < n; m++) {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        fw[i][j] = Math.min(fw[i][j], fw[i][m] + fw[m][j]);
                    }
                }
            }
            Dijkstra res = Dijkstra.run(r, 0);
            for (int v = 0; v < n; v++) {
                boolean fwReach = fw[0][v] < Long.MAX_VALUE / 8;
                if (fwReach != res.isReachable(v) || (fwReach && fw[0][v] != res.dist[v])) {
                    ok = false;
                }
            }
        }
        check("100 random directed graphs: Dijkstra == Floyd-Warshall (incl. unreachable)", ok);
    }
}
