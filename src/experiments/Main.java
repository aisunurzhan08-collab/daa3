package experiments;
import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class Main {
    static final long SEED = 42;
    static final int RUNS = 5;
    static final int WARMUP = 300;
    static final int INNER = 50;
    static final String[] NAMES = {"Small", "Medium", "Large"};
    static final int[] V = {10, 100, 1000};
    static final int[] E = {30, 500, 5000};
    static Object sink;

    interface Task {
        void run();
    }
    static double avgMicros(Task t) {
        for (int i = 0; i < WARMUP; i++) {
            t.run();
        }
        System.gc();
        long total = 0;
        for (int r = 0; r < RUNS; r++) {
            long start = System.nanoTime();
            for (int i = 0; i < INNER; i++) {
                t.run();
            }
            total += (System.nanoTime() - start) / INNER;
        }
        return total / (double) RUNS / 1000.0;
    }

    static String f(double x) {
        return String.format(Locale.US, "%.2f", x);
    }

    public static void main(String[] args) throws IOException {
        demo();
        runExperiments();
    }

    static void demo() {
        System.out.println("=========== DEMO on a small city ===========");
        String[] names = {"Center", "North", "East", "South", "West", "Airport"};
        Graph city = new Graph(6, false);
        city.addEdge(0, 1, 4);
        city.addEdge(0, 2, 3);
        city.addEdge(0, 3, 6);
        city.addEdge(0, 4, 5);
        city.addEdge(1, 2, 2);
        city.addEdge(2, 3, 1);
        city.addEdge(3, 4, 7);
        city.addEdge(2, 5, 8);
        city.addEdge(4, 5, 9);

        MSTResult k = Kruskal.run(city);
        MSTResult p = Prim.run(city);
        System.out.println("Kruskal MST weight = " + k.totalWeight + ", connected = " + k.connected);
        for (Edge e : k.edges) {
            System.out.println("   " + names[e.from] + " - " + names[e.to] + " (" + e.weight + ")");
        }
        System.out.println("Prim    MST weight = " + p.totalWeight + ", connected = " + p.connected);

        Dijkstra d = Dijkstra.run(city, 0);
        System.out.println("Dijkstra from " + names[0] + ":");
        for (int v = 0; v < 6; v++) {
            List<Integer> path = d.getPath(v);
            StringBuilder sb = new StringBuilder();
            for (int x : path) {
                sb.append(sb.length() == 0 ? "" : " -> ").append(names[x]);
            }
            System.out.println("   " + names[v] + ": dist = " + d.dist[v] + ", path = " + sb);
        }

        Graph deps = new Graph(5, true);
        deps.addEdge(0, 1, 1);
        deps.addEdge(0, 2, 1);
        deps.addEdge(1, 3, 1);
        deps.addEdge(2, 3, 1);
        deps.addEdge(3, 4, 1);
        TopologicalSort t = TopologicalSort.run(deps);
        System.out.println("Topological order (DAG = " + t.isDag + "): " + t.order);
        deps.addEdge(4, 1, 1);
        t = TopologicalSort.run(deps);
        System.out.println("After adding 4 -> 1: DAG = " + t.isDag + " (cycle detected, no ordering)");
        System.out.println("SCCs: " + SCC.run(deps).components);
        System.out.println();
    }

    static void runExperiments() throws IOException {
        Files.createDirectories(Paths.get("results/tables"));
        Files.createDirectories(Paths.get("results/plots"));
        StringBuilder all = new StringBuilder();

        double[] kruskalT = new double[3], primT = new double[3], dijkstraT = new double[3];
        double[] sccT = new double[3], topoT = new double[3], topoCycT = new double[3];

        List<String[]> mstRows = new ArrayList<>();
        List<String[]> dijRows = new ArrayList<>();
        List<String[]> topoRows = new ArrayList<>();
        List<String[]> sccRows = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            final Graph und = GraphGenerator.connectedUndirected(V[i], E[i], SEED);
            final Graph dag = GraphGenerator.directed(V[i], E[i], true, SEED);
            final Graph cyc = GraphGenerator.directed(V[i], E[i], false, SEED);

            kruskalT[i] = avgMicros(() -> sink = Kruskal.run(und));
            primT[i] = avgMicros(() -> sink = Prim.run(und));
            MSTResult k = Kruskal.run(und);
            MSTResult p = Prim.run(und);
            mstRows.add(new String[]{NAMES[i], "Kruskal", f(kruskalT[i]), "" + k.edgesExamined, "" + k.comparisons,
                    "" + k.dsuOperations, "-", "" + k.totalWeight, "" + k.connected});
            mstRows.add(new String[]{NAMES[i], "Prim", f(primT[i]), "" + p.edgesExamined, "-",
                    "-", "" + p.pqOperations, "" + p.totalWeight, "" + p.connected});

            dijkstraT[i] = avgMicros(() -> sink = Dijkstra.run(und, 0));
            Dijkstra d = Dijkstra.run(und, 0);
            dijRows.add(new String[]{NAMES[i], f(dijkstraT[i]), "" + d.edgesRelaxed, "" + d.pqOperations,
                    d.reachableCount() + "/" + V[i]});

            topoT[i] = avgMicros(() -> sink = TopologicalSort.run(dag));
            topoCycT[i] = avgMicros(() -> sink = TopologicalSort.run(cyc));
            TopologicalSort td = TopologicalSort.run(dag);
            TopologicalSort tc = TopologicalSort.run(cyc);
            topoRows.add(new String[]{NAMES[i], "DAG", f(topoT[i]), "" + td.edgesExamined, "" + td.isDag,
                    td.order.size() + "/" + V[i]});
            topoRows.add(new String[]{NAMES[i], "Cyclic", f(topoCycT[i]), "" + tc.edgesExamined, "" + tc.isDag,
                    tc.order.size() + "/" + V[i]});

            sccT[i] = avgMicros(() -> sink = SCC.run(cyc));
            double sccDagT = avgMicros(() -> sink = SCC.run(dag));
            SCC sc = SCC.run(cyc);
            SCC sd = SCC.run(dag);
            sccRows.add(new String[]{NAMES[i], "Cyclic", f(sccT[i]), "" + sc.edgesExamined,
                    "" + sc.components.size(), "" + sc.largestComponentSize()});
            sccRows.add(new String[]{NAMES[i], "DAG", f(sccDagT), "" + sd.edgesExamined,
                    "" + sd.components.size(), "" + sd.largestComponentSize()});
        }

        all.append(save("mst_kruskal_vs_prim",
                new String[]{"Graph", "Algorithm", "Avg time (us)", "Edges examined", "Sort comparisons",
                        "DSU ops (find+union)", "PQ ops (push+poll)", "MST weight", "Connected"}, mstRows));
        all.append(save("dijkstra",
                new String[]{"Graph", "Avg time (us)", "Edge relaxations", "PQ ops", "Reachable"}, dijRows));
        all.append(save("topological_sort",
                new String[]{"Graph", "Input", "Avg time (us)", "Edges examined", "Is DAG", "Vertices in order"},
                topoRows));
        all.append(save("scc",
                new String[]{"Graph", "Input", "Avg time (us)", "Edges examined", "SCC count", "Largest SCC"},
                sccRows));
        all.append(save("dsu_optimizations", dsuHeader(), dsuExperiment()));

        Files.writeString(Path.of("results/tables/all_tables.md"), all.toString());

        double[] xv = {V[0], V[1], V[2]};
        double[] xe = {E[0], E[1], E[2]};
        Map<String, double[]> series = new LinkedHashMap<>();
        series.put("Kruskal", kruskalT);
        series.put("Prim", primT);
        series.put("Dijkstra", dijkstraT);
        series.put("SCC (Tarjan)", sccT);
        series.put("Topological sort (DAG)", topoT);
        ChartUtil.lineChart("Execution time vs number of vertices", "Vertices V (log scale)",
                "Average time, microseconds (log scale)", xv, series, "results/plots/time_vs_vertices.png");
        ChartUtil.lineChart("Execution time vs number of edges", "Edges E (log scale)",
                "Average time, microseconds (log scale)", xe, series, "results/plots/time_vs_edges.png");

        Map<String, double[]> mstOnly = new LinkedHashMap<>();
        mstOnly.put("Kruskal", kruskalT);
        mstOnly.put("Prim", primT);
        ChartUtil.lineChart("Kruskal vs Prim", "Edges E (log scale)", "Average time, microseconds (log scale)",
                xe, mstOnly, "results/plots/kruskal_vs_prim.png");

        Map<String, double[]> topo = new LinkedHashMap<>();
        topo.put("Topological sort (DAG)", topoT);
        topo.put("Topological sort (cyclic)", topoCycT);
        topo.put("SCC (cyclic)", sccT);
        ChartUtil.lineChart("Connectivity algorithms", "Edges E (log scale)", "Average time, microseconds (log scale)",
                xe, topo, "results/plots/connectivity.png");

        System.out.println(all);
        System.out.println("Tables saved to results/tables, plots saved to results/plots");
    }

    static String[] dsuHeader() {
        return new String[]{"Variant", "find calls", "union calls", "Parent pointers followed", "Avg steps per find"};
    }

    static List<String[]> dsuExperiment() {
        Graph g = GraphGenerator.connectedUndirected(V[2], E[2], SEED);
        List<Edge> sorted = new ArrayList<>(g.getEdges());
        sorted.sort((a, b) -> Integer.compare(a.weight, b.weight));

        String[] names = {"No optimization", "Path compression only", "Union by rank only", "Both (used in project)"};
        boolean[][] flags = {{false, false}, {true, false}, {false, true}, {true, true}};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            DSU dsu = new DSU(g.getN(), flags[i][0], flags[i][1]);
            for (Edge e : sorted) {
                dsu.union(e.from, e.to);
            }
            for (int v = 0; v < g.getN(); v++) {
                dsu.find(v);
            }
            rows.add(new String[]{names[i], "" + dsu.findCalls, "" + dsu.unionCalls, "" + dsu.parentSteps,
                    f(dsu.parentSteps / (double) dsu.findCalls)});
        }
        return rows;
    }

    static String save(String name, String[] header, List<String[]> rows) throws IOException {
        StringBuilder csv = new StringBuilder(String.join(",", header)).append("\n");
        StringBuilder md = new StringBuilder("### " + name + "\n\n");
        md.append("| ").append(String.join(" | ", header)).append(" |\n|");
        for (int i = 0; i < header.length; i++) {
            md.append("---|");
        }
        md.append("\n");
        for (String[] r : rows) {
            csv.append(String.join(",", r)).append("\n");
            md.append("| ").append(String.join(" | ", r)).append(" |\n");
        }
        Files.writeString(Path.of("results/tables/" + name + ".csv"), csv.toString());
        return md.append("\n").toString();
    }
}
