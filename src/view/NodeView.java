package view;

import java.awt.Point;

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
    
    @Override
    public String toString() {
        return id;
    }
}