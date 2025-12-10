package common;

import java.awt.image.BufferedImage;
import server.Player; // ou String ownerSide si tu ne veux pas dépendre du serveur

public class ChessPiece {
    private PieceType type;
    private BufferedImage image;
    private Player joueur; // ou String ownerSide;
    private boolean isAlive = true;
    private int currentHP;
    private String name;

    public ChessPiece(PieceType type, BufferedImage image, Player joueur) {
        this.type = type;
        this.image = image;
        this.joueur = joueur;
        this.currentHP = type.getMaxHP();
        this.name = type.getDisplayName();
    }

    public PieceType getType() { return type; }
    public BufferedImage getImage() { return image; }
    public Player getJoueur() { return joueur; }
    public boolean isAlive() { return isAlive; }
    public int getCurrentHP() { return currentHP; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void takeDamage(int dmg) {
        currentHP -= dmg;
        if (currentHP <= 0) {
            isAlive = false;
            currentHP = 0;
        }
    }
}