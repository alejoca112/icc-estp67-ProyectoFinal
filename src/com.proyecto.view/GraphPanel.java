package com.proyecto.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// --- CLASES AUXILIARES (NODOS Y ARISTAS) ---
class NodeView {
    int x, y;
    String id;
    public static final int RADIUS = 15;

    public NodeView(int x, int y, String id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public boolean isClicked(int px, int py) {
        double distance = Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
        return distance <= RADIUS;
    }
}

class EdgeView {
    NodeView nodeA, nodeB;
    public EdgeView(NodeView a, NodeView b) { this.nodeA = a; this.nodeB = b; }
}

// --- CLASE DEL PANEL (LIENZO) ---
public class GraphPanel extends JPanel {
    private BufferedImage mapImage; // Aquí guardaremos la imagen YA ESTIRADA
    private ArrayList<NodeView> nodes;
    private ArrayList<EdgeView> edges;
    
    // Modos de edición
    private int currentMode = 0;
    public static final int MODE_ADD_NODE = 0;
    public static final int MODE_CONNECT = 1;

    private NodeView selectedNode = null; 

    public GraphPanel() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.setBackground(Color.WHITE);

        // Eventos del Mouse
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    public void setMapImage(BufferedImage originalImage) {
        if (originalImage != null) {
            // 1. Detectar el tamaño de pantalla
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) screenSize.getWidth();
            int height = (int) screenSize.getHeight();

            // 2. Crear una nueva imagen vacía del tamaño de la pantalla completa
            BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();

            // 3. Configurar calidad alta
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // 4. Dibujar la imagen original ESTIRADA para llenar todo el espacio
            g2d.drawImage(originalImage, 0, 0, width, height, null);
            g2d.dispose();

            // 5. Guardar esa imagen gigante como la oficial
            this.mapImage = scaledImage;
            
            // 6. Forzar al panel a tener ese tamaño gigante
            this.setPreferredSize(new Dimension(width, height));
            this.revalidate();
        }
        repaint();
    }

    // --- PINTAR EN PANTALLA ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Dibujar Mapa
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, this);
        } else {
            // Mensaje si no hay mapa
            g2.setColor(Color.RED);
            g2.drawString("NO SE HA CARGADO EL MAPA", 100, 100);
        }

        // 2. Dibujar Conexiones
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.BLACK);
        for (EdgeView e : edges) {
            g2.drawLine(e.nodeA.x, e.nodeA.y, e.nodeB.x, e.nodeB.y);
        }

        // 3. Dibujar Nodos
        for (NodeView n : nodes) {
            if (n == selectedNode) g2.setColor(Color.GREEN);
            else g2.setColor(Color.BLUE);
            
            int r = NodeView.RADIUS;
            g2.fillOval(n.x - r, n.y - r, r * 2, r * 2);
            
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(n.x - r, n.y - r, r * 2, r * 2);
            g2.drawString(n.id, n.x - 5, n.y + 5);
        }

        // 4. Texto de Modo
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String modeText = (currentMode == MODE_ADD_NODE) ? "MODO: CREAR NODOS (Clic en vacío)" : "MODO: CONECTAR (Clic nodo A -> Clic nodo B)";
        // Dibujamos un fondo blanco para que se lea el texto sobre el mapa
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRect(5, 5, 350, 25);
        g2.setColor(Color.BLACK);
        g2.drawString(modeText, 10, 22);
    }

    // --- LÓGICA DE CLICS ---
    private void handleMouseClick(int x, int y) {
        NodeView clickedNode = null;
        for (NodeView n : nodes) {
            if (n.isClicked(x, y)) {
                clickedNode = n;
                break;
            }
        }

        if (currentMode == MODE_ADD_NODE) {
            if (clickedNode == null) {
                String nodeId = "N" + (nodes.size() + 1);
                nodes.add(new NodeView(x, y, nodeId));
                repaint();
            }
        } else if (currentMode == MODE_CONNECT) {
            if (clickedNode != null) {
                if (selectedNode == null) selectedNode = clickedNode;
                else {
                    if (selectedNode != clickedNode) {
                        edges.add(new EdgeView(selectedNode, clickedNode));
                        selectedNode = null; 
                    }
                }
                repaint();
            } else {
                selectedNode = null;
                repaint();
            }
        }
    }

    public void setMode(int mode) {
        this.currentMode = mode;
        this.selectedNode = null;
        repaint();
    }
    
    public void clearGraph() {
        nodes.clear();
        edges.clear();
        selectedNode = null;
        repaint();
    }
}