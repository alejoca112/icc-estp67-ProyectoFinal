package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    private GraphPanel graphPanel;

    public MainFrame() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        initComponents();
        cargarMapaPorDefecto();
    }

    private void initComponents() {
        setTitle("Sistema de Rutas Profesional por BFS Y DFS");
        setSize(1280, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        graphPanel = new GraphPanel();
        add(new JScrollPane(graphPanel), BorderLayout.CENTER);

        // BARRA DE HERRAMIENTAS
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        toolBar.setBackground(new Color(240, 240, 245));

        // Creación de botones
        JButton btnLoadImg = crearBoton("Mapa");
        JButton btnLoadGraph = crearBoton("Cargar Grafo");
        JButton btnSave = crearBoton("Guardar");
        JButton btnClear = crearBoton("Limpiar");

        JToggleButton btnAdd = crearToggle("Crear Nodo");
        JToggleButton btnConnect = crearToggle("Conectar");

        JToggleButton btnStart = crearToggle("Inicio");
        JToggleButton btnEnd = crearToggle("Fin");
        JButton btnRunBFS = crearBoton("BFS");
        JButton btnRunDFS = crearBoton("DFS");

        // Grupo de botones
        ButtonGroup group = new ButtonGroup();
        group.add(btnAdd);
        group.add(btnConnect);
        group.add(btnStart);
        group.add(btnEnd);
        btnAdd.setSelected(true);

        // Añadiendo a barra
        toolBar.add(btnLoadImg);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(btnLoadGraph);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(btnSave);
        toolBar.addSeparator(new Dimension(20, 40));

        toolBar.add(btnAdd);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(btnConnect);
        toolBar.addSeparator(new Dimension(20, 40));

        toolBar.add(btnStart);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(btnEnd);
        toolBar.addSeparator(new Dimension(20, 40));

        toolBar.add(btnRunBFS);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(btnRunDFS);

        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnClear);

        add(toolBar, BorderLayout.NORTH);

        // ACCIONES
        btnAdd.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_ADD_NODE));
        btnConnect.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_CONNECT));
        btnStart.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_SELECT_START));
        btnEnd.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_SELECT_END));

        btnRunBFS.addActionListener(e -> graphPanel.runAlgorithm("BFS"));
        btnRunDFS.addActionListener(e -> graphPanel.runAlgorithm("DFS"));

        btnSave.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                if (graphPanel.saveGraph(fc.getSelectedFile()))
                    JOptionPane.showMessageDialog(this, "Guardado exitoso.");
            }
        });

        btnLoadGraph.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                if (graphPanel.loadGraph(fc.getSelectedFile()))
                    JOptionPane.showMessageDialog(this, "Grafo cargado correctamente.");
                else
                    JOptionPane.showMessageDialog(this, "Error al leer el archivo.");
            }
        });

        btnLoadImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                cargarImagen(fc.getSelectedFile());
        });

        btnClear.addActionListener(e -> graphPanel.clearGraph());
    }

    // Metodos de estilo
    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        estilizar(btn);
        return btn;
    }

    private JToggleButton crearToggle(String texto) {
        JToggleButton btn = new JToggleButton(texto);
        estilizar(btn);
        return btn;
    }

    private void estilizar(AbstractButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, 45));
        btn.setBackground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void cargarMapaPorDefecto() {
        File f = new File("assets/mapa.jpg");
        if (f.exists()) {
            cargarImagen(f);
            return;
        }

        f = new File("src/assets/mapa.jpg");
        if (f.exists()) {
            cargarImagen(f);
            return;
        }

        System.out.println("Aviso: No se encontró 'mapa.jpg' automáticamente.");
    }

    private void cargarImagen(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                graphPanel.setMapImage(img);

                SwingUtilities.invokeLater(() -> {
                    this.revalidate();
                    this.repaint();
                });

                System.out.println("Mapa cargado: " + file.getName());
            } else {
                JOptionPane.showMessageDialog(this, "Error: Imagen no válida.");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error crítico: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.d3d", "false");
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}