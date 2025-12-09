package common;

public class Paddle {
    private int x;
    private int y;
    private final int width = 15;
    private final int height = 100;
    private final int speed = 10;

    public Paddle(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void moveUp() {
        if (y > 0) {
            y -= speed;
        }
    }

    public void moveDown() {
        if (y < 600 - height) {
            y += speed;
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setY(int y) { this.y = y; }
}