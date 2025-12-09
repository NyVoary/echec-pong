package common;

import java.awt.*;

public class Echequier {
    private int x, y, cellWidth, cellHeight;

    public static final int ROWS = 2;
    public static final int COLS = 8;

    public Echequier(int x, int y, int cellWidth, int cellHeight) {
        this.x = x;
        this.y = y;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    public void draw(Graphics g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if ((row + col) % 2 == 0) {
                    g.setColor(new Color(220, 220, 220));
                } else {
                    g.setColor(new Color(100, 100, 100));
                }
                g.fillRect(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);
            }
        }
    }
}