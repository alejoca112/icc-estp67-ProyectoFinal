package model;

import view.EdgeView;
import view.NodeView;
import java.util.*;

public class Graph {
    private List<NodeView> nodeList;
    private Map<NodeView, List<EdgeView>> adjacencyMap;
    private List<EdgeView> allEdges;

    public Graph() {
        this.nodeList = new ArrayList<>();
        this.adjacencyMap = new HashMap<>();
        this.allEdges = new ArrayList<>();
    }

    public void addNode(NodeView node) {
        if (!adjacencyMap.containsKey(node)) {
            nodeList.add(node);
            adjacencyMap.put(node, new ArrayList<>());
        }
    }

    public boolean addEdge(NodeView a, NodeView b, double weight, boolean bidirectional) {
        if (a.equals(b)) return false; 
        if (!adjacencyMap.containsKey(a) || !adjacencyMap.containsKey(b)) return false;

        // 1. Crear conexión A -> B
        EdgeView edgeAB = new EdgeView(a, b, weight);
        
        // Evitar duplicados exactos
        boolean exists = false;
        for(EdgeView e : adjacencyMap.get(a)) if(e.nodeB.equals(b)) exists = true;
        
        if(!exists) {
            adjacencyMap.get(a).add(edgeAB);
            allEdges.add(edgeAB);
        }

        // 2. Si es bidireccional, crear también B -> A
        if (bidirectional) {
            EdgeView edgeBA = new EdgeView(b, a, weight);
            boolean existsRev = false;
            for(EdgeView e : adjacencyMap.get(b)) if(e.nodeB.equals(a)) existsRev = true;
            
            if (!existsRev) {
                adjacencyMap.get(b).add(edgeBA);
                allEdges.add(edgeBA); 
            }
        }
        return true;
    }

    public void removeNode(NodeView nodeToRemove) {
        if (nodeToRemove == null) return;
        for (NodeView neighbor : adjacencyMap.keySet()) {
            adjacencyMap.get(neighbor).removeIf(e -> e.nodeB.equals(nodeToRemove));
        }
        adjacencyMap.remove(nodeToRemove);
        nodeList.remove(nodeToRemove);
        allEdges.removeIf(e -> e.nodeA.equals(nodeToRemove) || e.nodeB.equals(nodeToRemove));
    }
    
    public NodeView getNodeById(String id) {
        for(NodeView n : nodeList) if(n.id.equals(id)) return n;
        return null;
    }

    public void clear() {
        nodeList.clear(); adjacencyMap.clear(); allEdges.clear();
    }

    public List<NodeView> getNodes() { return nodeList; }
    public List<EdgeView> getAllVisualEdges() { return allEdges; }
    public Map<NodeView, List<EdgeView>> getAdjacencyMap() { return adjacencyMap; }

    public List<NodeView> getNeighbors(NodeView node) {
        List<NodeView> neighbors = new ArrayList<>();
        List<EdgeView> edges = adjacencyMap.get(node);
        if (edges != null) {
            for (EdgeView e : edges) neighbors.add(e.nodeB);
        }
        return neighbors;
    }
}