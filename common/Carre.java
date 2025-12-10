package common;

public class Carre {
    private int row, col;
    private int x, y; // coordonnées pixel du coin supérieur gauche
    private ChessPiece piece; // null si vide

    public Carre(int row, int col, int x, int y) {
        this.row = row;
        this.col = col;
        this.x = x;
        this.y = y;
        this.piece = null;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getX() { return x; }
    public int getY() { return y; }
    public ChessPiece getPiece() { return piece; }
    public void setPiece(ChessPiece piece) { this.piece = piece; }
    public boolean hasPiece() { return piece != null && piece.isAlive(); }
}