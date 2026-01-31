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
        long startTime = System.nanoTime();

        Map<NodeView, List<NodeView>> adj = buildAdjacencyMap(allNodes, allEdges);
        List<NodeView> visitedOrder = new ArrayList<>();
        Map<NodeView, NodeView> parentMap = new HashMap<>();
        Stack<NodeView> stack = new Stack<>();
        Set<NodeView> visited = new HashSet<>();

        stack.push(start);

        boolean found = false;

        while (!stack.isEmpty()) {
            NodeView current = stack.pop();

            if (!visited.contains(current)) {
                visited.add(current);
                visitedOrder.add(current);

                if (current.equals(end)) {
                    found = true;
                    break;
                }

                for (NodeView neighbor : adj.getOrDefault(current, new ArrayList<>())) {
                    if (!visited.contains(neighbor)) {
                        parentMap.put(neighbor, current);
                        stack.push(neighbor);
                    }
                }
            }
        }

        long endTime = System.nanoTime();
        return new SearchResult(visitedOrder, reconstructPath(parentMap, end, found), (endTime - startTime));
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
