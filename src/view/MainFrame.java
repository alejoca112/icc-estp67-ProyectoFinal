package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame {

    private GraphPanel graphPanel;
    private JToggleButton btnAddNode, btnConnect; 
    private JLabel statusLabel;

    public MainFrame() {
        initComponents();
        cargarMapaPorDefecto(); 
    }

    private void initComponents() {
        setTitle("Sistema de Rutas - Las Vegas");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximizar ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setLayout(new BorderLayout());

        // --- Panel Central ---
        graphPanel = new GraphPanel();
        add(new JScrollPane(graphPanel), BorderLayout.CENTER);

        // --- Barra de Herramientas ---
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnLoad = new JButton("Cambiar Mapa");
        JButton btnClear = new JButton("Limpiar Todo");
        JButton btnSave = new JButton("Guardar Grafo");
        
        btnAddNode = new JToggleButton("Crear Nodos", true);
        btnConnect = new JToggleButton("Conectar Nodos");
        
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(btnAddNode);
        modeGroup.add(btnConnect);

        toolBar.add(btnLoad);
        toolBar.addSeparator();
        toolBar.add(btnAddNode);
        toolBar.add(btnConnect);
        toolBar.add(btnSave);
        toolBar.addSeparator();
        toolBar.add(btnClear);

        add(toolBar, BorderLayout.NORTH);
        
        statusLabel = new JLabel("Listo.");
        add(statusLabel, BorderLayout.SOUTH);

        // --- Eventos ---
        btnLoad.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                cargarImagen(fc.getSelectedFile());
            }
        });

        btnAddNode.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_ADD_NODE));
        btnConnect.addActionListener(e -> graphPanel.setMode(GraphPanel.MODE_CONNECT));
        btnClear.addActionListener(e -> graphPanel.clearGraph());
        //Boton Guardar
        btnSave.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("grafo_las_vegas.txt")); //Tipico Nombre
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File archivo = fc.getSelectedFile();
                if (graphPanel.saveGraph(archivo)) {
                    statusLabel.setText("Grafo guardado en: " + archivo.getName());
                    JOptionPane.showMessageDialog(this, "¡Archivo guardado correctamente!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar el archivo.");
                }
            }
        });
    }

    private void cargarMapaPorDefecto() {
        File archivoMapa = new File("assets/mapa.jpg");
        if (archivoMapa.exists()) {
            cargarImagen(archivoMapa);
        } else {
            File archivoAlternativo = new File("src/assets/mapa.jpg");
            if (archivoAlternativo.exists()) cargarImagen(archivoAlternativo);
        }
    }

    private void cargarImagen(File file) {
        try {
            BufferedImage imagen = ImageIO.read(file);
            // ESTA LLAMADA ACTIVA EL ESTIRADO EN GRAPH PANEL
            graphPanel.setMapImage(imagen); 
            statusLabel.setText("Mapa cargado: " + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) {}
            new MainFrame().setVisible(true);
        });
    }
}