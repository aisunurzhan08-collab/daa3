package connectivity;
import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
public class SCC {
    public final List<List<Integer>> components = new ArrayList<>();
    public long edgesExamined = 0;

    private final Graph g;
    private final int[] index;
    private final int[] low;
    private final boolean[] onStack;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private int time = 0;

    private SCC(Graph g) {
        this.g = g;
        this.index = new int[g.getN()];
        this.low = new int[g.getN()];
        this.onStack = new boolean[g.getN()];
        Arrays.fill(index, -1);
    }

    public static SCC run(Graph g) {
        SCC scc = new SCC(g);
        for (int v = 0; v < g.getN(); v++) {
            if (scc.index[v] == -1) {
                scc.dfs(v);
            }
        }
        return scc;
    }

    private void dfs(int u) {
        index[u] = low[u] = time++;
        stack.push(u);
        onStack[u] = true;

        for (Edge e : g.getAdj(u)) {
            edgesExamined++;
            int v = e.to;
            if (index[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], index[v]);
            }
        }

        if (low[u] == index[u]) {
            List<Integer> comp = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                comp.add(w);
            } while (w != u);
            components.add(comp);
        }
    }

    public int largestComponentSize() {
        int max = 0;
        for (List<Integer> c : components) {
            max = Math.max(max, c.size());
        }
        return max;
    }
}
