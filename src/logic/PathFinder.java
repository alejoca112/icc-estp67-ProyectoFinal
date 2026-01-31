package logic;

import view.EdgeView;
import view.NodeView;
import java.util.*;

public class PathFinder {

    public static class SearchResult {
        public List<NodeView> visitedOrder;
        public List<NodeView> finalPath;
        public long executionTime; // Tiempo en nanosegundos

        public SearchResult(List<NodeView> v, List<NodeView> p, long time) {
            this.visitedOrder = v;
            this.finalPath = p;
            this.executionTime = time;
        }
    }

    public static SearchResult bfs(NodeView start, NodeView end, List<NodeView> allNodes, List<EdgeView> allEdges) {
        //Por Completar
        return new SearchResult(new ArrayList<>(), new ArrayList<>(), 0);
    }

    public static SearchResult dfs(NodeView start, NodeView end, List<NodeView> allNodes, List<EdgeView> allEdges) {
        //Por Completar
        return new SearchResult(new ArrayList<>(), new ArrayList<>(), 0);
    }

    private static List<NodeView> reconstructPath(Map<NodeView, NodeView> parentMap, NodeView end, boolean found) {
        List<NodeView> path = new ArrayList<>();
        if (!found) return path;

        NodeView curr = end;
        while (curr != null) {
            path.add(0, curr);
            curr = parentMap.get(curr);
        }
        return path;
    }

    private static Map<NodeView, List<NodeView>> buildAdjacencyMap(List<NodeView> nodes, List<EdgeView> edges) {
        Map<NodeView, List<NodeView>> map = new HashMap<>();
        for (NodeView n : nodes) map.put(n, new ArrayList<>());
        for (EdgeView e : edges) {
            map.get(e.nodeA).add(e.nodeB);
            map.get(e.nodeB).add(e.nodeA);
        }
        return map;
    }
}
