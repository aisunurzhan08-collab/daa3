package mst;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
public class Kruskal {

    public static MSTResult run(Graph g) {
        MSTResult res = new MSTResult();
        List<Edge> sorted = new ArrayList<>(g.getEdges());
        long[] cmp = {0};
        sorted.sort((a, b) -> {
            cmp[0]++;
            return Integer.compare(a.weight, b.weight);
        });
        res.comparisons = cmp[0];
        DSU dsu = new DSU(g.getN());
        for (Edge e : sorted) {
            res.edgesExamined++;
            if (dsu.union(e.from, e.to)) {
                res.edges.add(e);
                res.totalWeight += e.weight;
                if (res.edges.size() == g.getN() - 1) {
                    break;
                }
            }
        }
        res.components = dsu.getComponents();
        res.connected = (res.components == 1);
        res.dsuOperations = dsu.findCalls + dsu.unionCalls;
        return res;
    }
}
