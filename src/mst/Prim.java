package mst;
import graph.Edge;
import graph.Graph;
import java.util.PriorityQueue;
public class Prim {

    public static MSTResult run(Graph g) {
        MSTResult res = new MSTResult();
        int n = g.getN();
        boolean[] visited = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> Integer.compare(a.weight, b.weight));

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }
            res.components++;
            visited[start] = true;
            for (Edge e : g.getAdj(start)) {
                pq.add(e);
                res.pqOperations++;
            }
            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                res.pqOperations++;
                res.edgesExamined++;
                if (visited[e.to]) {
                    continue;
                }
                visited[e.to] = true;
                res.edges.add(e);
                res.totalWeight += e.weight;
                for (Edge next : g.getAdj(e.to)) {
                    if (!visited[next.to]) {
                        pq.add(next);
                        res.pqOperations++;
                    }
                }
            }
        }
        res.connected = (res.components == 1);
        return res;
    }
}
