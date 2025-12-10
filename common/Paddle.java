package common;

public class Paddle {
    private int x;
    private int y;
    private int width;
    private int height;

    public Paddle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Bouge vers la gauche, en respectant la largeur du panel
    public void moveLeft(int distance, int panelWidth) {
        x = Math.max(0, x - distance);
    }

    // Bouge vers la droite, en respectant la largeur du panel
    public void moveRight(int distance, int panelWidth) {
        x = Math.min(panelWidth - width, x + distance);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
}