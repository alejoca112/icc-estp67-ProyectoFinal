package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    private GraphPanel graphPanel;

    public MainFrame() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) {}
        initComponents();
        cargarMapaPorDefecto(); 
    }

    private void initComponents() {
        setTitle("Sistema de Rutas BFS Y DFS");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        graphPanel = new GraphPanel();
        add(new JScrollPane(graphPanel), BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        toolBar.setBackground(new Color(245, 245, 250));

        
        // Archivo
        JButton btnLoadImg = crearBotonIcono("Mapa", "icon_mapa.png");
        JButton btnLoadGraph = crearBotonIcono("Abrir", "icon_upload.png");
        JButton btnSave = crearBotonIcono("Guardar", "icon_save.png");
        JButton btnResults = crearBotonIcono("Reporte", "icon_results.png");

        JToggleButton btnAdd = crearToggleIcono("Nodo", "icon_add.png");
        JToggleButton btnConnect = crearToggleIcono("Conectar", "icon_connect.png");
        
        // Selección de Puntos
        JToggleButton btnStart = crearToggleIcono("Inicio", "icon_start.png");
        JToggleButton btnEnd = crearToggleIcono("Fin", "icon_end.png");
        
        // Borrador
        JToggleButton btnDelete = crearToggleIcono("Borrar", "icon_deleteNode.png");

        JCheckBox chkDirected = new JCheckBox("Unidireccional");
        JCheckBox chkHideEdges = new JCheckBox("Ocultar Líneas");
        chkDirected.setOpaque(false);
        chkHideEdges.setOpaque(false);

        // Algoritmos
        JButton btnRunBFS = crearBotonIcono("BFS", "icon_run.png");
        JButton btnRunDFS = crearBotonIcono("DFS", "icon_run.png");
        JButton btnClear = crearBotonIcono("Limpiar", "icon_clear.png");

        // AGRUPAR MODOS
        ButtonGroup group = new ButtonGroup();
        group.add(btnAdd); 
        group.add(btnConnect); 
        group.add(btnStart); 
        group.add(btnEnd); 
        group.add(btnDelete);
        btnAdd.setSelected(true);

        // --- AÑADIR A LA BARRA ---
        toolBar.add(btnLoadImg);
        toolBar.add(btnLoadGraph);
        toolBar.add(btnSave);
        toolBar.addSeparator();
        toolBar.add(btnResults);
        toolBar.addSeparator();
        
        toolBar.add(btnAdd);
        toolBar.add(btnConnect);
        toolBar.add(chkDirected);
        toolBar.addSeparator();
        
        toolBar.add(btnStart);
        toolBar.add(btnEnd);
        toolBar.addSeparator();
        
        toolBar.add(btnDelete);
        toolBar.add(chkHideEdges);
        toolBar.addSeparator();
        
        toolBar.add(btnRunBFS);
        toolBar.add(btnRunDFS);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnClear);

        add(toolBar, BorderLayout.NORTH);

        // --- EVENTOS (Listeners) ---
        
        // Modos
        btnAdd.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_ADD_NODE));
        btnConnect.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_CONNECT));
        btnStart.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_SELECT_START));
        btnEnd.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_SELECT_END));
        btnDelete.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_DELETE_NODE));

        // Opciones
        chkDirected.addActionListener(e -> graphPanel.setDirectedMode(chkDirected.isSelected()));
        chkHideEdges.addActionListener(e -> graphPanel.setShowEdges(!chkHideEdges.isSelected()));

        // Algoritmos y Archivos
        btnRunBFS.addActionListener(e -> graphPanel.runAlgorithm("BFS"));
        btnRunDFS.addActionListener(e -> graphPanel.runAlgorithm("DFS"));
        btnResults.addActionListener(e -> mostrarResultados());
        btnClear.addActionListener(e -> graphPanel.clearGraph());

        btnSave.addActionListener(e -> {
             JFileChooser fc = new JFileChooser();
             if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) graphPanel.saveGraph(fc.getSelectedFile());
        });
        btnLoadGraph.addActionListener(e -> {
             JFileChooser fc = new JFileChooser();
             if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                 if(graphPanel.loadGraph(fc.getSelectedFile())) JOptionPane.showMessageDialog(this, "Grafo cargado.");
             }
        });
        btnLoadImg.addActionListener(e -> {
             JFileChooser fc = new JFileChooser();
             if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) cargarImagen(fc.getSelectedFile());
        });
    }

    private ImageIcon cargarIcono(String nombreArchivo) {
        try {
            File file = new File("assets/" + nombreArchivo);
            if (!file.exists()) file = new File("src/assets/" + nombreArchivo);
            
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                Image scaled = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {}
        return null; // Si falla, botón sin icono
    }

    private JButton crearBotonIcono(String texto, String iconoFile) {
        JButton btn = new JButton(texto);
        btn.setIcon(cargarIcono(iconoFile));
        estilizar(btn);
        return btn;
    }

    private JToggleButton crearToggleIcono(String texto, String iconoFile) {
        JToggleButton btn = new JToggleButton(texto);
        btn.setIcon(cargarIcono(iconoFile));
        estilizar(btn);
        return btn;
    }

    private void estilizar(AbstractButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(2, 10, 2, 10)); // Padding
    }

    private void mostrarResultados() {
        JTextArea textArea = new JTextArea(20, 50);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        File file = new File("reporte_tiempos.csv");
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                textArea.append("ALG | TIEMPO | PASOS | DISTANCIA\n");
                textArea.append("----------------------------------\n");
                while (scanner.hasNextLine()) textArea.append(scanner.nextLine().replace(",", " | ") + "\n");
            } catch (IOException e) { textArea.setText("Error leyendo archivo."); }
        } else { textArea.setText("Sin registros."); }
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Historial", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cargarMapaPorDefecto() {
        File f = new File("assets/mapa.jpg");
        if(!f.exists()) f = new File("src/assets/mapa.jpg");
        if(f.exists()) cargarImagen(f);
    }

    private void cargarImagen(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if(img != null) graphPanel.setMapImage(img);
        } catch (IOException ex) {}
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.d3d", "false"); 
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}