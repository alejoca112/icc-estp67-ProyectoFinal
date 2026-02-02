package view;

import logic.PathFinder;
import model.Graph; // Importamos el modelo Graph
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class GraphPanel extends JPanel {
    private BufferedImage mapImage;
    // REEMPLAZO: Usamos el objeto Graph en lugar de listas sueltas
    private Graph graph;

    public static final int MODE_ADD_NODE = 0;
    public static final int MODE_CONNECT = 1;
    // Eliminamos modos de seleccion de inicio/fin porque ahora es con clic derecho
    private int currentMode = MODE_ADD_NODE;

    private NodeView selectedNodeForConnection = null;
    private NodeView startNode = null;
    private NodeView endNode = null;

    private Timer animationTimer;
    private List<NodeView> animationQueue;
    private List<NodeView> visitedNodes;
    private List<NodeView> finalPath;

    // Menús contextuales
    private JPopupMenu nodePopupMenu;
    private JPopupMenu edgePopupMenu;
    private NodeView popupNodeTarget;
    private EdgeView popupEdgeTarget;

    public GraphPanel() {
        this.graph = new Graph();
        this.visitedNodes = new ArrayList<>();
        this.finalPath = new ArrayList<>();
        this.animationQueue = new ArrayList<>();
        
        this.setBackground(Color.DARK_GRAY);
        this.setPreferredSize(new Dimension(1200, 800));

        setupPopupMenus();

        animationTimer = new Timer(40, e -> stepAnimation());

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleMouseAction(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handleMouseAction(e); }
        };
        this.addMouseListener(ma);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { handleMouseMove(e.getX(), e.getY()); }
        });
    }

    private void setupPopupMenus() {
        // --- Menú para Nodos ---
        nodePopupMenu = new JPopupMenu();
        JMenuItem itemStart = new JMenuItem("Establecer como INICIO");
        JMenuItem itemEnd = new JMenuItem("Establecer como FIN");
        JMenuItem itemDelete = new JMenuItem("Eliminar Nodo");

        itemStart.addActionListener(e -> { startNode = popupNodeTarget; repaint(); });
        itemEnd.addActionListener(e -> { endNode = popupNodeTarget; repaint(); });
        itemDelete.addActionListener(e -> deleteNode(popupNodeTarget));

        nodePopupMenu.add(itemStart);
        nodePopupMenu.add(itemEnd);
        nodePopupMenu.addSeparator();
        nodePopupMenu.add(itemDelete);

        // --- Menú para Aristas ---
        edgePopupMenu = new JPopupMenu();
        JMenuItem itemWeight = new JMenuItem("Editar Peso/Distancia");
        itemWeight.addActionListener(e -> editEdgeWeight(popupEdgeTarget));
        edgePopupMenu.add(itemWeight);
    }

    private void handleMouseAction(MouseEvent e) {
        if (e.isPopupTrigger()) {
            handleRightClick(e.getX(), e.getY());
        } else if (e.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isLeftMouseButton(e)) {
            handleLeftClick(e.getX(), e.getY());
        }
        
    }
    private void handleRightClick(int x, int y) {
        NodeView clickedNode = getClickedNode(x, y);
        if (clickedNode != null) {
            popupNodeTarget = clickedNode;
            nodePopupMenu.show(e.getComponent(), x, y);
            return;
        }
        EdgeView clickedEdge = getClickedEdge(x, y);
        if (clickedEdge != null) {
            popupEdgeTarget = clickedEdge;
            edgePopupMenu.show(e.getComponent(), x, y);
            return;
        }
        cancelSelection();
    }

    

    private void handleLeftClick(int x, int y) {
        NodeView clicked = getClickedNode(x, y);
        switch (currentMode) {
            case MODE_ADD_NODE:
                if (clicked == null) {
                    graph.addNode(new NodeView(x, y, "N" + (graph.getNodes().size() + 1)));
                    repaint();
                }
                break;
            case MODE_CONNECT:
                if (clicked != null) {
                    if (selectedNodeForConnection == null) selectedNodeForConnection = clicked;
                    else {
                        graph.addEdge(selectedNodeForConnection, clicked, 1.0);
                        selectedNodeForConnection = null;
                    }
                    repaint();
                }
                break;
        }
    }

    // --- Funciones del Menú Contextual ---
    private void deleteNode(NodeView target) {
        if(target == null) return;
        if(startNode == target) startNode = null;
        if(endNode == target) endNode = null;
        graph.removeNode(target);
        repaint();
    }

    private void editEdgeWeight(EdgeView target) {
        if(target == null) return;
        String input = JOptionPane.showInputDialog(this, "Ingrese nueva distancia para la conexión:", target.weight);
        try {
            double newWeight = Double.parseDouble(input);
            // Actualizar el peso en ambas direcciones (grafo no dirigido)
            target.weight = newWeight;
            for(EdgeView reverse : graph.getAdjacencyMap().get(target.nodeB)) {
                if(reverse.nodeB.equals(target.nodeA)) {
                    reverse.weight = newWeight;
                    break;
                }
            }
            repaint();
        } catch (NumberFormatException | NullPointerException ex) {
             // Ignorar entrada inválida
        }
    }

    // --- PINTADO ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (mapImage != null) g2.drawImage(mapImage, 0, 0, this);

        // 1. Dibujar Aristas y Pesos
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        DecimalFormat df = new DecimalFormat("#.#");

        for (EdgeView e : graph.getAllVisualEdges()) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(e.nodeA.x, e.nodeA.y, e.nodeB.x, e.nodeB.y);
            
            // Dibujar peso en el centro de la arista
            int midX = (e.nodeA.x + e.nodeB.x) / 2;
            int midY = (e.nodeA.y + e.nodeB.y) / 2;
            g2.setColor(Color.BLUE);
            g2.drawString(df.format(e.weight), midX, midY);
        }

        // 2. Dibujar Ruta Final
        if (!animationQueue.isEmpty() || !visitedNodes.isEmpty()) {
            if (!animationTimer.isRunning() && !finalPath.isEmpty()) {
                 g2.setStroke(new BasicStroke(6));
                 g2.setColor(new Color(255, 0, 50));
                 for (int i = 0; i < finalPath.size() - 1; i++) {
                     g2.drawLine(finalPath.get(i).x, finalPath.get(i).y, finalPath.get(i+1).x, finalPath.get(i+1).y);
                 }
            }
        }

        // 3. Dibujar Nodos
        for (NodeView n : graph.getNodes()) {
            if (n == startNode) g2.setColor(Color.GREEN);
            else if (n == endNode) g2.setColor(Color.MAGENTA);
            else if (finalPath.contains(n) && !animationTimer.isRunning()) g2.setColor(Color.RED);
            else if (visitedNodes.contains(n)) g2.setColor(Color.ORANGE);
            else if (n == selectedNodeForConnection) g2.setColor(Color.CYAN);
            else g2.setColor(Color.BLUE);

            int r = NodeView.RADIUS;
            g2.fillOval(n.x - r, n.y - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(n.x - r, n.y - r, r * 2, r * 2);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(n.id, n.x - 5, n.y + 5);
        }
        
        // Panel Info
        drawInfoPanel(g2);
    }

    private void drawInfoPanel(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(10, 10, 550, 40, 20, 20);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String txt = "Modo: " + (currentMode == MODE_ADD_NODE ? "Crear" : "Conectar") + 
                     " (Clic Der. para opciones)";
        if(startNode != null) txt += " | Inicio: " + startNode.id;
        if(endNode != null) txt += " | Fin: " + endNode.id;
        g2.drawString(txt, 25, 36);
    }

    // --- Ejecución y Utilidades ---
    public void runAlgorithm(String type) {
        if (startNode == null || endNode == null) {
            JOptionPane.showMessageDialog(this, "Usa click derecho en los nodos para definir Inicio y Fin.");
            return;
        }
        visitedNodes.clear(); finalPath.clear(); repaint();

        PathFinder.SearchResult result = type.equals("BFS") ? 
            PathFinder.bfs(startNode, endNode, graph) : 
            PathFinder.dfs(startNode, endNode, graph);

        DecimalFormat df = new DecimalFormat("#.##");
        logExecution(type, result.executionTime, result.finalPath.size(), df.format(result.totalDistance));
        animationQueue = result.visitedOrder;
        finalPath = result.finalPath; 
        
        if (animationQueue.isEmpty() && finalPath.isEmpty()) {
             JOptionPane.showMessageDialog(this, "No existe ruta.");
             return;
        }
        animationTimer.start();
    }

    private void stepAnimation() {
        if (animationQueue != null && !animationQueue.isEmpty()) {
            visitedNodes.add(animationQueue.remove(0));
            repaint();
        } else {
            animationTimer.stop();
            repaint(); 
            if(!finalPath.isEmpty()) {
                DecimalFormat df = new DecimalFormat("#.##");
                 double totalDist = PathFinder.bfs(startNode, endNode, graph).totalDistance; // Recalcular rápido
                 JOptionPane.showMessageDialog(this, "Ruta encontrada!\nDistancia Total: " + df.format(totalDist));
            }
        }
    }

    // --- Helpers de detección y persistencia ---
    private NodeView getClickedNode(int x, int y) {
        for (NodeView n : graph.getNodes()) if (n.isClicked(x, y)) return n;
        return null;
    }
    
    private EdgeView getClickedEdge(int x, int y) {
        for (EdgeView e : graph.getAllVisualEdges()) if (e.isClicked(x, y)) return e;
        return null;
    }
    
    public void setMode(int mode) { this.currentMode = mode; selectedNodeForConnection = null; repaint(); }
    public void clearGraph() { graph.clear(); startNode=null; endNode=null; visitedNodes.clear(); finalPath.clear(); repaint(); }

    public void setMapImage(BufferedImage img) { 
        if (img == null) return;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage scaled = new BufferedImage(screenSize.width, screenSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(img, 0, 0, screenSize.width, screenSize.height, null);
        g2d.dispose();
        this.mapImage = scaled;
        this.setPreferredSize(screenSize);
        revalidate(); repaint();
    }

    public boolean saveGraph(File file) {  
         try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("NODES");
            for (NodeView n : graph.getNodes()) pw.println(n.id + "," + n.x + "," + n.y);
            pw.println("EDGES");
            // Guardar con peso
            for (EdgeView e : graph.getAllVisualEdges()) pw.println(e.nodeA.id + "," + e.nodeB.id + "," + e.weight);
            return true;
        } catch (IOException e) { return false; }
    }

    public boolean loadGraph(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            clearGraph();
            String line; boolean readingNodes = false; boolean readingEdges = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("NODES")) { readingNodes = true; readingEdges = false; continue; }
                if (line.equals("EDGES")) { readingNodes = false; readingEdges = true; continue; }
                if (readingNodes) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) graph.addNode(new NodeView(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), parts[0]));
                } else if (readingEdges) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        NodeView nA = graph.getNodeById(parts[0]);
                        NodeView nB = graph.getNodeById(parts[1]);
                        double w = parts.length > 2 ? Double.parseDouble(parts[2]) : 1.0;
                        if (nA != null && nB != null) graph.addEdge(nA, nB, w);
                    }
                }
            }
            repaint(); return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private void logExecution(String alg, long timeNs, int steps, String dist) {
        try (FileWriter fw = new FileWriter("reporte_tiempos.csv", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(alg + "," + timeNs + "ns," + steps + " pasos, Distancia:" + dist);
        } catch (IOException e) { }
    }
    private void handleMouseMove(int x, int y) {
        boolean overNode = getClickedNode(x, y) != null;
        boolean overEdge = getClickedEdge(x, y) != null;
        setCursor((overNode || overEdge) ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
    }
    private void cancelSelection() { selectedNodeForConnection = null; repaint(); }
}