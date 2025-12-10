package common;

import java.awt.*;

public class Echequier {
    private int x, y, cellWidth, cellHeight;
    private int cols; // Nombre de colonnes variable

    public static final int ROWS = 2;

    private String[] rowOwners = {"J2", "J2"};

    public Echequier(int x, int y, int cellWidth, int cellHeight, int cols) {
        this.x = x;
        this.y = y;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.cols = cols;
    }

    public void setRowOwners(String ownerTop, String ownerBottom) {
        rowOwners[0] = ownerTop;
        rowOwners[1] = ownerBottom;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getCols() {
        return cols;
    }

    public void draw(Graphics g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < cols; col++) {
                // Couleur alternée
                if ((row + col) % 2 == 0) {
                    g.setColor(new Color(220, 220, 200));
                } else {
                    g.setColor(new Color(150, 50, 50));
                }
                g.fillRect(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);

                // Dessiner le texte du propriétaire
                g.setColor(Color.BLACK);
                String owner = rowOwners[row];
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(owner);
                int textHeight = fm.getHeight();
                int tx = x + col * cellWidth + (cellWidth - textWidth) / 2;
                int ty = y + row * cellHeight + (cellHeight + textHeight) / 2 - 4;
                g.drawString(owner, tx, ty);
            }
        }
    }
}