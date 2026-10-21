package connectivity;
import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
public class TopologicalSort {
    public final List<Integer> order = new ArrayList<>();
    public boolean isDag;
    public long edgesExamined = 0;

    public static TopologicalSort run(Graph g) {
        TopologicalSort res = new TopologicalSort();
        int n = g.getN();
        int[] inDegree = new int[n];
        for (int u = 0; u < n; u++) {
            for (Edge e : g.getAdj(u)) {
                inDegree[e.to]++;
                res.edgesExamined++;
            }
        }
        Queue<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                queue.add(v);
            }
        }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            res.order.add(u);
            for (Edge e : g.getAdj(u)) {
                res.edgesExamined++;
                inDegree[e.to]--;
                if (inDegree[e.to] == 0) {
                    queue.add(e.to);
                }
            }
        }
        res.isDag = (res.order.size() == n);
        return res;
    }
}
