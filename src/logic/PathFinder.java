package logic;

import view.EdgeView;
import view.NodeView;
import model.Graph;
import java.util.*;

public class PathFinder {

    public static class SearchResult {
        public List<NodeView> visitedOrder;
        public List<NodeView> finalPath;
        public long executionTime;
        public double totalDistance;

        public SearchResult(List<NodeView> v, List<NodeView> p, long time, double dist) {
            this.visitedOrder = v;
            this.finalPath = p;
            this.executionTime = time;
            this.totalDistance = dist;
        }
    }

    public static SearchResult bfs(NodeView start, NodeView end, Graph graph) {
        long startTime = System.nanoTime();
        List<NodeView> visitedOrder = new ArrayList<>();
        Map<NodeView, NodeView> parentMap = new HashMap<>();
        Queue<NodeView> queue = new LinkedList<>();
        Set<NodeView> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        boolean found = false;

        while (!queue.isEmpty()) {
            NodeView current = queue.poll();
            visitedOrder.add(current);
            if (current.equals(end)) { found = true; break; }

            for (NodeView neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        
        long endTime = System.nanoTime();
        List<NodeView> path = reconstructPath(parentMap, end, found);
        double dist = calculatePathDistance(path, graph);
        return new SearchResult(visitedOrder, path, (endTime - startTime), dist);
    }

    public static SearchResult dfs(NodeView start, NodeView end, Graph graph) {
        long startTime = System.nanoTime();
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
                if (current.equals(end)) { found = true; break; }

                for (NodeView neighbor : graph.getNeighbors(current)) {
                    if (!visited.contains(neighbor)) {
                        parentMap.put(neighbor, current);
                        stack.push(neighbor);
                    }
                }
            }
        }
        long endTime = System.nanoTime();
        List<NodeView> path = reconstructPath(parentMap, end, found);
        double dist = calculatePathDistance(path, graph);
        return new SearchResult(visitedOrder, path, (endTime - startTime), dist);
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
    
    private static double calculatePathDistance(List<NodeView> path, Graph graph) {
        if(path.isEmpty() || path.size() < 2) return 0.0;
        double total = 0;
        Map<NodeView, List<EdgeView>> adj = graph.getAdjacencyMap();
        for(int i=0; i < path.size()-1; i++) {
            NodeView u = path.get(i);
            NodeView v = path.get(i+1);
            for(EdgeView e : adj.get(u)) {
                if(e.nodeB.equals(v)) {
                    total += e.weight;
                    break;
                }
            }
        }
        return total;
    }
}
