package mst;
import graph.Edge;
import java.util.ArrayList;
import java.util.List;
public class MSTResult {
    public final List<Edge> edges = new ArrayList<>();
    public long totalWeight = 0;
    public boolean connected = false;
    public int components = 0;

    public long edgesExamined = 0;
    public long comparisons = 0;
    public long pqOperations = 0;
    public long dsuOperations = 0;
}
