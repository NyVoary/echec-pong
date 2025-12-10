package common;

import java.awt.image.BufferedImage;
import server.Player; // ou String ownerSide si tu ne veux pas dépendre du serveur

public class ChessPiece {
    private PieceType type;
    private BufferedImage image;
    private Player joueur; // ou String ownerSide;
    private boolean isAlive = true;
    private int currentHP;
    private int row, col;
    private boolean isWhite;

    public ChessPiece(PieceType type, BufferedImage image, Player joueur, int row, int col, boolean isWhite) {
        this.type = type;
        this.image = image;
        this.joueur = joueur;
        this.currentHP = type.getMaxHP();
        this.row = row;
        this.col = col;
        this.isWhite = isWhite;
    }

    public PieceType getType() { return type; }
    public BufferedImage getImage() { return image; }
    public Player getJoueur() { return joueur; }
    public boolean isAlive() { return isAlive; }
    public int getCurrentHP() { return currentHP; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isWhite() { return isWhite; }

    public void takeDamage(int dmg) {
        currentHP -= dmg;
        if (currentHP <= 0) {
            isAlive = false;
            currentHP = 0;
        }
    }
}