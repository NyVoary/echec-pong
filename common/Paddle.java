package common;

public class Paddle {
    private int x;
    private int y;
    private final int width = 180;
    private final int height = 15;

    public Paddle(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Bouge de 'distance' pixels vers la gauche
    public void moveLeft(int distance) {
        if (x - distance >= 0) {
            x -= distance;
        } else {
            x = 0; // Ne pas sortir du bord
        }
    }

    // Bouge de 'distance' pixels vers la droite
    public void moveRight(int distance) {
        if (x + distance <= 480 - width) {
            x += distance;
        } else {
            x = 480 - width; // Ne pas sortir du bord
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}