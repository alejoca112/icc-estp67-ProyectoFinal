package view;

import logic.PathFinder;
import model.Graph;
import view.EdgeView;
import view.NodeView;

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
    private Graph graph;

    // --- MODOS DE EDICIÓN (RECUPERADOS) ---
    public static final int MODE_ADD_NODE = 0;
    public static final int MODE_CONNECT = 1;
    public static final int MODE_SELECT_START = 2;
    public static final int MODE_SELECT_END = 3;
    public static final int MODE_DELETE_NODE = 4;  // Borrador
    
    private int currentMode = MODE_ADD_NODE;
    private boolean isDirectedMode = false; // Checkbox Unidireccional
    private boolean showEdges = true;       // Checkbox Ocultar Conexiones

    private NodeView selectedNodeForConnection = null;
    private NodeView startNode = null;
    private NodeView endNode = null;

    private Timer animationTimer;
    private List<NodeView> animationQueue;
    private List<NodeView> visitedNodes;
    private List<NodeView> finalPath;

    public GraphPanel() {
        this.graph = new Graph();
        this.visitedNodes = new ArrayList<>();
        this.finalPath = new ArrayList<>();
        this.animationQueue = new ArrayList<>();
        
        this.setBackground(Color.DARK_GRAY);
        this.setPreferredSize(new Dimension(1200, 800));

        animationTimer = new Timer(40, e -> stepAnimation());

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleMouseAction(e); }
        };
        this.addMouseListener(ma);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { handleMouseMove(e.getX(), e.getY()); }
        });
    }

    // --- SETTERS ---
    public void setMode(int mode) { 
        this.currentMode = mode; 
        this.selectedNodeForConnection = null; 
        repaint(); 
    }
    
    public void setDirectedMode(boolean isDirected) {
        this.isDirectedMode = isDirected;
    }

    public void setShowEdges(boolean show) {
        this.showEdges = show;
        repaint();
    }

    private void handleMouseAction(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            NodeView clicked = getClickedNode(e.getX(), e.getY());
            
            switch (currentMode) {
                case MODE_ADD_NODE:
                    if (clicked == null) {
                        graph.addNode(new NodeView(e.getX(), e.getY(), "N" + (graph.getNodes().size() + 1)));
                        repaint();
                    }
                    break;
                    
                case MODE_CONNECT:
                    if (clicked != null) {
                        if (selectedNodeForConnection == null) selectedNodeForConnection = clicked;
                        else {
                            boolean bidirectional = !isDirectedMode; 
                            graph.addEdge(selectedNodeForConnection, clicked, 1.0, bidirectional);
                            selectedNodeForConnection = null;
                        }
                        repaint();
                    }
                    break;

                case MODE_SELECT_START:
                    if (clicked != null) {
                        startNode = clicked;
                        repaint();
                    }
                    break;

                case MODE_SELECT_END:
                    if (clicked != null) {
                        endNode = clicked;
                        repaint();
                    }
                    break;
                    
                case MODE_DELETE_NODE:
                    if (clicked != null) {
                        if(startNode == clicked) startNode = null;
                        if(endNode == clicked) endNode = null;
                        graph.removeNode(clicked);
                        repaint();
                    }
                    break;
            }
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            EdgeView clickedEdge = getClickedEdge(e.getX(), e.getY());
            if (clickedEdge != null) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem item = new JMenuItem("Editar Distancia/Peso");
                item.addActionListener(ev -> editEdgeWeight(clickedEdge));
                menu.add(item);
                menu.show(this, e.getX(), e.getY());
            } else {
                // Cancelar selección si se hace clic derecho en el vacío
                selectedNodeForConnection = null;
                repaint();
            }
        }
    }

    private void editEdgeWeight(EdgeView target) {
        String input = JOptionPane.showInputDialog(this, "Ingrese distancia:", target.weight);
        try {
            double val = Double.parseDouble(input);
            target.weight = val;
            // Actualizar inverso si existe
            for(EdgeView e : graph.getAllVisualEdges()) {
                if(e.nodeA == target.nodeB && e.nodeB == target.nodeA) {
                    e.weight = val;
                }
            }
            repaint();
        } catch(Exception ex) {}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (mapImage != null) g2.drawImage(mapImage, 0, 0, this);

        // 1. Dibujar Aristas
        if (showEdges) {
            g2.setStroke(new BasicStroke(2));
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            DecimalFormat df = new DecimalFormat("#.#");

            for (EdgeView e : graph.getAllVisualEdges()) {
                g2.setColor(Color.BLACK);
                drawArrowLine(g2, e.nodeA.x, e.nodeA.y, e.nodeB.x, e.nodeB.y, 15, 5);
                
                // Peso
                int midX = (e.nodeA.x + e.nodeB.x) / 2;
                int midY = (e.nodeA.y + e.nodeB.y) / 2;
                g2.setColor(Color.BLUE);
                g2.drawString(df.format(e.weight), midX, midY - 5);
            }
        }

        // 2. Ruta Final
        if (!finalPath.isEmpty() && !animationTimer.isRunning()) {
            g2.setStroke(new BasicStroke(5));
            g2.setColor(new Color(255, 0, 50));
            for (int i = 0; i < finalPath.size() - 1; i++) {
                g2.drawLine(finalPath.get(i).x, finalPath.get(i).y, finalPath.get(i+1).x, finalPath.get(i+1).y);
            }
        }

        // 3. Nodos
        for (NodeView n : graph.getNodes()) {
            if (n == startNode) g2.setColor(Color.GREEN);
            else if (n == endNode) g2.setColor(Color.MAGENTA);
            else if (visitedNodes.contains(n)) g2.setColor(Color.ORANGE);
            else if (n == selectedNodeForConnection) g2.setColor(Color.CYAN);
            else g2.setColor(Color.BLUE);

            int r = NodeView.RADIUS;
            g2.fillOval(n.x - r, n.y - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(n.x - r, n.y - r, r * 2, r * 2);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(n.id, n.x - 5, n.y + 5);
        }
        
        // Info Panel
        g2.setColor(new Color(0,0,0,180));
        g2.fillRoundRect(10, 10, 400, 30, 20, 20);
        g2.setColor(Color.WHITE);
        g2.drawString("Modo: " + getModeString(), 20, 30);
    }
    
    private String getModeString() {
        switch(currentMode) {
            case MODE_ADD_NODE: return "Crear Nodos";
            case MODE_CONNECT: return "Conectar " + (isDirectedMode ? "(Uni)" : "(Bi)");
            case MODE_SELECT_START: return "Seleccionar INICIO";
            case MODE_SELECT_END: return "Seleccionar FIN";
            case MODE_DELETE_NODE: return "BORRADOR";
            default: return "";
        }
    }

    private void drawArrowLine(Graphics2D g, int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;
        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;
        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;
        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};
        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
    }

    // --- Algoritmos y Utilidades ---
    public void runAlgorithm(String type) {
        if (startNode == null || endNode == null) {
            JOptionPane.showMessageDialog(this, "Selecciona Inicio y Fin usando los botones."); return;
        }
        visitedNodes.clear(); finalPath.clear(); repaint();
        PathFinder.SearchResult result = type.equals("BFS") ? PathFinder.bfs(startNode, endNode, graph) : PathFinder.dfs(startNode, endNode, graph);
        logExecution(type, result.executionTime, result.finalPath.size(), String.format("%.2f", result.totalDistance));
        animationQueue = result.visitedOrder;
        finalPath = result.finalPath;
        if(animationQueue.isEmpty()) JOptionPane.showMessageDialog(this, "No hay ruta.");
        else animationTimer.start();
    }
    
    private void stepAnimation() {
        if (!animationQueue.isEmpty()) { visitedNodes.add(animationQueue.remove(0)); repaint(); }
        else { animationTimer.stop(); repaint(); }
    }
    
    private void logExecution(String alg, long timeNs, int steps, String dist) {
        try (FileWriter fw = new FileWriter("reporte_tiempos.csv", true); PrintWriter pw = new PrintWriter(fw)) {
            pw.println(alg + "," + timeNs + "," + steps + "," + dist);
        } catch (IOException e) {}
    }

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
                        if (nA != null && nB != null) graph.addEdge(nA, nB, w, true);
                    }
                }
            }
            repaint(); return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    public void clearGraph() { graph.clear(); startNode=null; endNode=null; visitedNodes.clear(); finalPath.clear(); repaint(); }
    private NodeView getClickedNode(int x, int y) { for(NodeView n : graph.getNodes()) if(n.isClicked(x, y)) return n; return null; }
    private EdgeView getClickedEdge(int x, int y) { for(EdgeView e : graph.getAllVisualEdges()) if(e.isClicked(x, y)) return e; return null; }
    private void handleMouseMove(int x, int y) { setCursor(getClickedNode(x,y)!=null ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR)); }
}