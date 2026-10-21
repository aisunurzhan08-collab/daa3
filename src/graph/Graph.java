package graph;
import java.util.ArrayList;
import java.util.List;
public class Graph {
    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }
    public void addEdge(int u, int v, int weight) {
        edges.add(new Edge(u, v, weight));
        adj.get(u).add(new Edge(u, v, weight));
        if (!directed) {
            adj.get(v).add(new Edge(v, u, weight));
        }
    }
    public int getN() {
        return n;
    }
    public int getEdgeCount() {
        return edges.size();
    }
    public boolean isDirected() {
        return directed;
    }
    public List<Edge> getEdges() {
        return edges;
    }
    public List<Edge> getAdj(int u) {
        return adj.get(u);
    }
}
