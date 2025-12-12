package common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
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
        // Charge l'image automatiquement si elle n'est pas fournie
        if (image == null) {
            try {
                String path = "pieces/" + type.getImageFileName(isWhite);
                this.image = ImageIO.read(new File(path));
            } catch (IOException e) {
                System.err.println("Image manquante: " + type.getImageFileName(isWhite));
                this.image = null;
            }
        } else {
            this.image = image;
        }
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

    public void setCurrentHP(int hp) {
        this.currentHP = hp;
        this.isAlive = hp > 0;
    }
    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }
}