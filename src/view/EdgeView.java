package view;

public class EdgeView {
    public NodeView nodeA, nodeB;
    public double weight;

    public EdgeView(NodeView a, NodeView b) {
        this(a, b, 1.0);
    }

    public EdgeView(NodeView a, NodeView b, double weight) {
        this.nodeA = a;
        this.nodeB = b;
        this.weight = weight;
    }

    // Método para detectar si un clic está cerca de la línea
    public boolean isClicked(int px, int py) {
        double distance = java.awt.geom.Line2D.ptSegDist(nodeA.x, nodeA.y, nodeB.x, nodeB.y, px, py);
        return distance < 5.0; // Tolerancia de 5 pixeles
    }
}
