package view;

import java.awt.Point;
import java.util.Objects;

public class NodeView {
    public int x, y;
    public String id;
    public static final int RADIUS = 15;

    public NodeView(int x, int y, String id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public boolean isClicked(int px, int py) {
        return Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2)) <= RADIUS;
    }

    public Point getPoint() {
        return new Point(x, y);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeView nodeView = (NodeView) o;
        return Objects.equals(id, nodeView.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}