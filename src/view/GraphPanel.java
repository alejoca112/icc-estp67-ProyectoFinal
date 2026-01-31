package view;

import logic.PathFinder; 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GraphPanel extends JPanel {
    private BufferedImage mapImage;
    private ArrayList<NodeView> nodes;
    private ArrayList<EdgeView> edges;

    public static final int MODE_ADD_NODE = 0;
    public static final int MODE_CONNECT = 1;
    public static final int MODE_SELECT_START = 2; 
    public static final int MODE_SELECT_END = 3;   
    private int currentMode = MODE_ADD_NODE;

    private NodeView selectedNode = null; 
    private NodeView startNode = null;    
    private NodeView endNode = null;      

    private Timer animationTimer;
    private List<NodeView> animationQueue; 
    private List<NodeView> visitedNodes;   
    private List<NodeView> finalPath;      

    public GraphPanel() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.visitedNodes = new ArrayList<>();
        this.finalPath = new ArrayList<>();
        this.animationQueue = new ArrayList<>(); 
        
        this.setBackground(Color.DARK_GRAY);

        this.setPreferredSize(new Dimension(1200, 800));

        animationTimer = new Timer(125, e -> stepAnimation());

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { handleMouseMove(e.getX(), e.getY()); }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    cancelSelection();
                    return;
                }
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    public void setMapImage(BufferedImage originalImage) {
        if (originalImage != null) {
            //Obtiene el tamaño para poder colocar la imagen en escala 
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int targetW = screenSize.width;
            int targetH = screenSize.height;

            BufferedImage scaledImage = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Aqui inerto la imagen estirada
            g2d.drawImage(originalImage, 0, 0, targetW, targetH, null);
            g2d.dispose();

            this.mapImage = scaledImage;
            
            // 5. Ajustar panel
            this.setPreferredSize(new Dimension(targetW, targetH));
            this.revalidate(); 
            this.repaint();
            System.out.println("--> IMAGEN REDIMENSIONADA A PANTALLA COMPLETA: " + targetW + "x" + targetH);
        } else {
            System.err.println("--> ERROR: Imagen nula");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, this);
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
            g2.drawString("Sistema Listo. Cargue un mapa para comenzar.", 50, 100);
        }

        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.BLACK);
        for (EdgeView e : edges) g2.drawLine(e.nodeA.x, e.nodeA.y, e.nodeB.x, e.nodeB.y);

        if (!animationQueue.isEmpty() || !visitedNodes.isEmpty()) {
            if (!animationTimer.isRunning() && !finalPath.isEmpty()) {
                 g2.setStroke(new BasicStroke(6));
                 g2.setColor(new Color(255, 0, 50));
                 for (int i = 0; i < finalPath.size() - 1; i++) {
                     NodeView n1 = finalPath.get(i);
                     NodeView n2 = finalPath.get(i+1);
                     g2.drawLine(n1.x, n1.y, n2.x, n2.y);
                 }
            }
        }

        for (NodeView n : nodes) {
            if (n == startNode) g2.setColor(Color.GREEN);
            else if (n == endNode) g2.setColor(Color.MAGENTA);
            else if (finalPath.contains(n) && !animationTimer.isRunning()) g2.setColor(Color.RED);
            else if (visitedNodes.contains(n)) g2.setColor(Color.ORANGE);
            else if (n == selectedNode) g2.setColor(Color.CYAN);
            else g2.setColor(Color.BLUE);

            int r = NodeView.RADIUS;
            g2.fillOval(n.x - r, n.y - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(n.x - r, n.y - r, r * 2, r * 2);
            
            // Texto
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(Color.WHITE); 
            g2.drawString(n.id, n.x - 4, n.y + 6); // Sombra
            g2.setColor(Color.BLACK);
            g2.drawString(n.id, n.x - 5, n.y + 5);
        }
        
        // Panel
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(10, 10, 500, 40, 20, 20);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        String txt = "Modo: " + getModeString();
        if(startNode != null) txt += " | Inicio: " + startNode.id;
        if(endNode != null) txt += " | Fin: " + endNode.id;
        g2.drawString(txt, 25, 36);
    }

    public void runAlgorithm(String type) {
        if (startNode == null || endNode == null) {
            JOptionPane.showMessageDialog(this, "Selecciona Inicio y Fin primero.");
            return;
        }
        visitedNodes.clear();
        finalPath.clear();
        repaint();

        PathFinder.SearchResult result;
        if (type.equals("BFS")) result = PathFinder.bfs(startNode, endNode, nodes, edges);
        else result = PathFinder.dfs(startNode, endNode, nodes, edges);

        logExecution(type, result.executionTime, result.finalPath.size());
        animationQueue = result.visitedOrder;
        finalPath = result.finalPath; 
        
        if (animationQueue.isEmpty() && finalPath.isEmpty()) {
             JOptionPane.showMessageDialog(this, "No existe ruta entre estos nodos.");
             return;
        }
        animationTimer.start();
    }

    private void logExecution(String alg, long timeNs, int steps) {
        try (FileWriter fw = new FileWriter("reporte_tiempos.csv", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(alg + "," + timeNs + "," + steps);
        } catch (IOException e) { System.err.println("Error CSV: " + e.getMessage()); }
    }

    private void stepAnimation() {
        if (animationQueue != null && !animationQueue.isEmpty()) {
            visitedNodes.add(animationQueue.remove(0));
            repaint();
        } else {
            animationTimer.stop();
            repaint(); 
        }
    }

    private String getModeString() {
        switch(currentMode) {
            case MODE_ADD_NODE: return "Crear Nodos";
            case MODE_CONNECT: return "Conectar";
            case MODE_SELECT_START: return "Sel. INICIO";
            case MODE_SELECT_END: return "Sel. FIN";
            default: return "";
        }
    }

    private void handleMouseClick(int x, int y) {
        NodeView clicked = null;
        for (NodeView n : nodes) if (n.isClicked(x, y)) { clicked = n; break; }

        switch (currentMode) {
            case MODE_ADD_NODE:
                if (clicked == null) {
                    nodes.add(new NodeView(x, y, "N" + (nodes.size() + 1)));
                    repaint();
                }
                break;
            case MODE_CONNECT:
                if (clicked != null) {
                    if (selectedNode == null) selectedNode = clicked;
                    else {
                        if (selectedNode != clicked) edges.add(new EdgeView(selectedNode, clicked));
                        selectedNode = null;
                    }
                    repaint();
                }
                break;
            case MODE_SELECT_START:
                if (clicked != null) { startNode = clicked; repaint(); }
                break;
            case MODE_SELECT_END:
                if (clicked != null) { endNode = clicked; repaint(); }
                break;
        }
    }

    private void handleMouseMove(int x, int y) {
        boolean over = false;
        for(NodeView n : nodes) if(n.isClicked(x, y)) { over = true; break; }
        setCursor(over ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    private void cancelSelection() { selectedNode = null; repaint(); }

    public void setMode(int mode) { this.currentMode = mode; selectedNode = null; repaint(); }
    public void clearGraph() { nodes.clear(); edges.clear(); startNode=null; endNode=null; visitedNodes.clear(); finalPath.clear(); repaint(); }
    
    public boolean saveGraph(File file) {  
         try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("NODES");
            for (NodeView n : nodes) pw.println(n.id + "," + n.x + "," + n.y);
            pw.println("EDGES");
            for (EdgeView e : edges) pw.println(e.nodeA.id + "," + e.nodeB.id);
            return true;
        } catch (IOException e) { return false; }
    }

    public boolean loadGraph(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            clearGraph();
            String line;
            boolean readingNodes = false;
            boolean readingEdges = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("NODES")) { readingNodes = true; readingEdges = false; continue; }
                if (line.equals("EDGES")) { readingNodes = false; readingEdges = true; continue; }

                if (readingNodes) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) nodes.add(new NodeView(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), parts[0]));
                } else if (readingEdges) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        NodeView nA = getNodeById(parts[0]);
                        NodeView nB = getNodeById(parts[1]);
                        if (nA != null && nB != null) edges.add(new EdgeView(nA, nB));
                    }
                }
            }
            repaint();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private NodeView getNodeById(String id) {
        for(NodeView n : nodes) if(n.id.equals(id)) return n;
        return null;
    }
}